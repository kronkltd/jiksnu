(ns jiksnu.actions
  (:require [ciste.commands :refer [add-command!]]
            [ciste.core :refer [defaction with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [ciste.routes :refer [resolve-routes]]
            [clojure.core.incubator :refer [dissoc-in]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.channels :as ch]
            [jiksnu.handlers :as handler]
            [jiksnu.predicates :as pred]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.time :as lt]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+ try+]]))

(defonce connections (ref {}))

(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

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
  ;; (log/debugf "closed connection: %s" connection-id)
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
  (trace/trace :websocket:connections:established 1)
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
    (or
     (try
       ((resolve-routes [@pred/*page-predicates*]
                        @pred/*page-matchers*) request)
       (catch Throwable ex
         (trace/trace :errors:handled ex)))
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
     (trace/trace :actions:invoked:error ex)
     (log/error ex "Actions error")
     {:message (str ex)
      :action "error"})))

(defaction confirm
  [action model id]
  (when-let [item (get-model model id)]
    {:item item
     :action action}))

(defn init-handlers
  []

  ;; (l/receive-all (trace/select-probes "*:create:in") #'handler/event)
  ;; (l/receive-all (trace/select-probes "*:created")   #'handler/created)
  ;; (l/receive-all (trace/select-probes "*:field:set")  #'handler/field-set)
  ;; (l/receive-all (trace/select-probes "*:linkAdded") #'handler/linkAdded)

  (doseq [[kw v]
          [
           [:actions:invoked               #'handler/actions-invoked]
           ;; [:activities:pushed             #'handler/activities-pushed]
           ;; [:ciste:filters:run             #'handler/event]
           ;; [:ciste:predicate:tested        #'handler/event]
           ;; [:ciste:matcher:tested          #'handler/matcher-test]
           ;; [:ciste:matcher:matched         #'handler/event]
           ;; [:ciste:route:matched           #'handler/event]
           ;; [:ciste:sections:run            #'handler/event]
           ;; [:ciste:views:run               #'handler/event]
           ;; [:conversations:pushed          #'handler/conversations-pushed]
           ;; [:entry:parsed                  #'handler/entry-parsed]
           [:http-client:error             #'handler/http-client-error]
           [:errors:handled                #'handler/errors]
           ;; [:feed:parsed                   #'handler/feed-parsed]
           ;; [:lamina-default-executor:stats #'handler/event]
           ;; [:person:parsed                 #'handler/person-parsed]
           [:resource:realized             #'handler/resource-realized]
           [:resource:failed               #'handler/resource-failed]
           ]]
    (l/receive-all (trace/probe-channel kw) v))

  ;; (l/receive-all
  ;;  (l/sample-every
  ;;   {:period (lt/seconds 30)}
  ;;   (trace/probe-channel :lamina-default-executor:stats))
  ;;  #'handler/event)

  )

(defonce receivers (init-handlers))
