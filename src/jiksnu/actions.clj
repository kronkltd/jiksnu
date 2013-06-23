(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [*environment*]]
        [ciste.core :only [defaction with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.initializer :only [definitializer]]
        [ciste.routes :only [resolve-routes]]
        [ciste.sections.default :only [link-to]]
        [clojure.core.incubator :only [dissoc-in]]
        [clojure.data.json :only [read-json]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require #_[clj-airbrake.core :as airbrake]
            [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.predicates :as pred]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [lamina.core :as l]
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
    #_(log/error ex)
    #_(.printStackTrace ex)
    #_(airbrake/notify
       "d61e18dac7af78220e52697e5b08dd5a"
       (name @*environment*)
       ;; "development"
       "/"
       ex
       {:url "foo"
        :params (into {} (map (fn [[k v]] {k (pr-str v)})
                              (:environment data)))})))


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
        connection-id (abdera/new-id)]


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

(defaction get-model
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
        (throw+ "page not found"))))

(defaction invoke-action
  [model-name action-name id]
  (try+
    (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
      (require action-ns)

      (if-let [action (ns-resolve action-ns (symbol action-name))]
        (let [body (with-serialization :command (filter-action action id))
              response {:message "action invoked"
                        :model model-name
                        :action action-name
                        :id id
                        :body body}]
          (trace/trace "actions:invoked" response)
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

(defn init-handlers
  []
  (l/receive-all (trace/probe-channel "errors:handled") handle-errors)

  (l/receive-all (trace/probe-channel "actions:invoked")
                 (fn [response]
                   (s/increment "actions invoked")))

  (l/receive-all (trace/probe-channel "activities:pushed")
                 (fn [response]
                   (log/infof "sending update notification to connection: %s"
                              (:connection-id response))
                   (s/increment "activities pushed")))

  (l/receive-all (trace/probe-channel "conversations:pushed")
                 (fn [response]
                   (log/infof "sending update notification to connection: %s"
                              (:connection-id response))
                   (s/increment "conversations pushed"))))

(definitializer
  (init-handlers))
