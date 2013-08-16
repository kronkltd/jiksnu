(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [*environment* config]]
        [ciste.core :only [defaction with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.routes :only [resolve-routes]]
        [clojure.core.incubator :only [dissoc-in]]
        [clojure.data.json :only [read-json]]
        [clojure.pprint :only [pprint]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clj-airbrake.core :as airbrake]
            [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.predicates :as pred]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.time :as lt]
            [lamina.trace :as trace])
  (:import clojure.lang.ExceptionInfo))

(defonce connections (ref {}))

(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

(defn handle-errors
  [ex]
  (let [data (if (instance? ExceptionInfo ex)
               (.getData ex) {})]
    (log/error ex)
    (.printStackTrace ex)
    (when (config :airbrake :enabled)
      (let [options {:url "foo"
                     :params (into {} (map (fn [[k v]] {k (pr-str v)})
                                           (:environment data)))}]
        (airbrake/notify
         (config :airbrake :key)
         (name @*environment*)
         "/" ex options)))))


(defn transform-activities
  [connection-id e]
  (let [response {:action "model-updated"
                  :connection-id connection-id
                  :type "activity"
                  :body (:records e)}]
    (trace/trace "activities:pushed" response)
    (json/json-str response)))

(defn transform-conversations
  [connection-id e]
  (let [response {:action "page-add"
                  :connection-id connection-id
                  :name "public-timeline"
                  :body (:_id (:records e))}]
    (trace/trace "conversations:pushed" response)
    (json/json-str response)))

(defn connection-closed
  [id connection-id]
  (log/debugf "closed connection: %s" connection-id)
  (dosync
   (alter connections #(dissoc-in % [id connection-id]))))

(defaction alert-all
  [message]
  (doseq [ch (all-channels)]
    (let [response (json/json-str {:action "add notice"
                                   :message message})]
      (l/enqueue ch response))))

(defaction connect
  [ch]
  (s/increment "websocket connections established")
  (let [user-id (:_id (session/current-user))
        connection-id (util/new-id)]


    (let [response-channel (l/channel*
                            :description (format "Outgoing messages for %s" connection-id))]

      (dosync
       (alter connections #(assoc-in % [user-id connection-id] response-channel)))

      (l/siphon
       (l/map* (partial transform-activities connection-id)
               ch/posted-activities)
       response-channel)

      (l/siphon
       (l/map* (partial transform-conversations connection-id)
               ch/posted-conversations)
       response-channel)

      (l/join response-channel ch))

    (l/on-closed ch (partial connection-closed user-id connection-id))
    connection-id))

(defn get-model
  [model-name id]
  (let [model-ns (symbol (str "jiksnu.model." model-name))]
    (require model-ns)
    (let [fetcher (ns-resolve model-ns 'fetch-by-id)]
      ;; (log/debugf "getting model %s(%s)" model-name id)
      (fetcher id))))

(defaction get-page
  [page-name & args]
  ;; (log/debugf "Getting page: %s" page-name)
  (let [request {:format :page
                 :serialization :page
                 :name page-name
                 :args args}]
    (or ((resolve-routes [@pred/*page-predicates*]
                         @pred/*page-matchers*) request)
        (throw+ "page not found"))))

(defaction get-sub-page
  [item page-name & args]
  ;; (log/debugf "Getting sub-page: %s(%s) => %s" (class item) (:_id item) page-name)
  (let [request {:format :page
                 :serialization :page
                 :name page-name
                 :item item
                 :args args}
        route-handler (resolve-routes [@pred/*sub-page-predicates*]
                                      @pred/*sub-page-matchers*)]
    (or (route-handler request)
        {:action "error"
         :page page-name
         :message "sub page not found"})))

(defaction invoke-action
  [model-name action-name id & [options]]
  (try+
    (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
      (require action-ns)

      (if-let [action (ns-resolve action-ns (symbol action-name))]
        (let [body (with-serialization :command
                     (with-format :clj
                       (filter-action action id)))
              response {:message "action invoked"
                        :model model-name
                        :action action-name
                        :id id
                        :body body}]
          (trace/trace :actions:invoked response)
          response)
        (do
          (log/warnf "could not find action for: %s(%s) => %s"
                     model-name id action-name)
          {:message (format "action not found: %s" action-name)
           :action "error"})))
    (catch RuntimeException ex
      (log/spy :info &throw-context)
      (trace/trace "errors:handled" ex))))

(defaction confirm
  [action model id]
  (when-let [item (get-model model id)]
    {:item item
     :action action}))

(add-command! "invoke-action" #'invoke-action)
(add-command! "connect"       #'connect)
(add-command! "get-model"     #'get-model)
(add-command! "get-page"      #'get-page)
(add-command! "get-sub-page"  #'get-sub-page)

(defn handle-actions-invoked
  [response]
  (s/increment "actions invoked"))

(defn handle-activities-pushed
  [response]
  (log/infof "sending update notification to connection: %s"
             (:connection-id response))
  (s/increment "activities pushed"))

(defn handle-conversations-pushed
  [response]
  (log/infof "sending update notification to connection: %s"
             (:connection-id response))
  (s/increment "conversations pushed"))

(defn handle-created
  [item]
  (log/infof "created:\n\n%s\n%s"
             (class item)
             (with-out-str (pprint item))))

(defn handle-field-set
  [[item field value]]
  (log/infof "setting %s(%s): (%s = %s)"
             (.getSimpleName (class item))
             (:_id item)
             field
             (pr-str value)))

(defn handle-linkAdded
  [[item link]]
  (log/infof "adding link %s(%s) => %s"
             (.getSimpleName (class item))
             (:_id item)
             (pr-str link)))

(defn handle-feed-parsed
  [response]
  (log/infof "parsed feed: %s"
             (with-out-str
               (pprint response))))

(defn handle-entry-parsed
  [entry]
  (log/infof "Parsing Entry:\n\n%s\n"
             (str entry)))

(defn handle-person-parsed
  [person]
  (log/infof "Parsing Person:\n\n%s\n"
             (str person)))

(defn handle-resource-realized
  [[item res]]
  (log/infof "Resource Realized: %s" (:url item)))

(defn handle-resource-failed
  [[item res]]
  (log/infof "Resource Failed: %s" (:url item)))

(defn handle-event
  [event]
  ;; (println "\n")
  (let [message (with-out-str
                  (pprint event))]
   (println message)))

(defn handle-http-client-error
  [event]
  (log/errorf "Http Client Error: %s" (pr-str event)))

(defn init-handlers
  []

  ;; (l/receive-all (trace/select-probes "*:create:in") #'handle-event)
  ;; (l/receive-all (trace/select-probes "*:created")   #'handle-created)
  ;; (l/receive-all (trace/select-probes "*:field:set")  #'handle-field-set)
  ;; (l/receive-all (trace/select-probes "*:linkAdded") #'handle-linkAdded)

  (doseq [[kw v]
          [
           [:actions:invoked               #'handle-actions-invoked]
           ;; [:activities:pushed             #'handle-activities-pushed]
           ;; [:ciste:filters:run             #'handle-event]
           ;; [:ciste:sections:run            #'handle-event]
           ;; [:ciste:views:run               #'handle-event]
           ;; [:conversations:pushed          #'handle-conversations-pushed]
           ;; [:entry:parsed                  #'handle-entry-parsed]
           [:http-client:error             #'handle-http-client-error]
           [:errors:handled                #'handle-errors]
           ;; [:feed:parsed                   #'handle-feed-parsed]
           ;; [:lamina-default-executor:stats #'handle-event]
           ;; [:person:parsed                 #'handle-person-parsed]
           [:resource:realized             #'handle-resource-realized]
           [:resource:failed               #'handle-resource-failed]
           ]]
    (l/receive-all (trace/probe-channel kw) v))

  ;; (l/receive-all
  ;;  (l/sample-every
  ;;   {:period (lt/seconds 30)}
  ;;   (trace/probe-channel :lamina-default-executor:stats))
  ;;  #'handle-event)

  )

(defonce receivers (init-handlers))
