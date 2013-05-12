(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [*environment*]]
        [ciste.core :only [defaction with-serialization]]
        [ciste.filters :only [deffilter filter-action]]
        [ciste.routes :only [resolve-routes]]
        [ciste.views :only [defview]]
        [clojure.core.incubator :only [dissoc-in]]
        [clojure.data.json :only [read-json]])
  (:require #_[clj-airbrake.core :as airbrake]
            [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [lamina.core :as l]
            [lamina.trace :as trace])
  (:import clojure.lang.ExceptionInfo))

(defaction invoke-action
  [model-name action-name id]
  (try
    (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
      (require action-ns)

      (if-let [action (ns-resolve action-ns (symbol action-name))]
        (let [body (with-serialization :command (filter-action action id))]
          (let [response {:message "action invoked"
                          :model model-name
                          :action action-name
                          :id id
                          :body body}]
            (s/increment "actions invoked")
            (trace/trace "actions:invoked" response)
            response))
        (do
          (log/warnf "could not find action for: %s(%s) => %s"
                     model-name id action-name)
          {:message "action not found"
           :action "error"})))
    (catch RuntimeException ex
      (trace/trace "errors:handled" ex))))

(defn handle-errors
  [ex]
  (let [data (if (instance? ExceptionInfo ex)
               (.getData ex) {})]
    (.printStackTrace ex)
    #_(airbrake/notify
       "d61e18dac7af78220e52697e5b08dd5a"
       (name @*environment*)
       ;; "development"
       "/"
       ex
       {:url "foo"
        :params (into {} (map (fn [[k v]] {k (pr-str v)})
                              (:environment data)))})))

(deffilter #'invoke-action :command
  [action request]
  (apply action (:args request)))

(defview #'invoke-action :json
  [request data]
  {:body data})

(add-command! "invoke-action" #'invoke-action)

(defonce connections (ref {}))

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
  (let [response {:action "model-updated"
                  :connection-id connection-id
                  :type "conversation"
                  :body (:records e)}]
    (trace/trace "conversations:pushed" response)
    (json/json-str response)))

(defn connection-closed
  [id connection-id]
  (log/debugf "closed connection: %s" connection-id)
  (dosync
   (alter connections #(dissoc-in % [id connection-id]))))

(defaction connect
  [ch]
  (s/increment "websocket connections established")
  (let [user-id (:_id (session/current-user))
        connection-id (abdera/new-id)]
    (dosync
     (alter connections #(assoc-in % [user-id connection-id] ch)))

    (-> (partial transform-activities connection-id)
        (l/map* ch/posted-activities)
        (l/siphon ch))

    (-> (partial transform-conversations connection-id)
        (l/map* ch/posted-conversations)
        (l/siphon ch))

    (l/on-closed ch (partial connection-closed user-id connection-id))
    connection-id))

(deffilter #'connect :command
  [action request]
  (action (:channel request)))

(defview #'connect :json
  [request response]
  {:body {:connection-id response}})

(add-command! "connect" #'connect)

(defaction get-model
  [model-name id]
  ;; (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
  ;;   (require action-ns)
  ;;   (let [maker (ns-resolve action-ns 'maker)]

      (log/debugf "getting model %s(%s)" model-name id)
      (let [item ((templates/make-fetch-by-id model-name identity) id)]
        (log/spy :info item)
        )

      ;; ))
  )

(deffilter #'get-model :command
  [action request]
  (let [[model-name id] (:args request)]
    (action model-name id)))

(defview #'get-model :json
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})

(add-command! "get-model" #'get-model)


(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

(defn alert-all
  [message]
  (doseq [ch (all-channels)]
    (let [response (json/json-str {:action "add notice"
                                   :message message})]
      (l/enqueue ch response))))

(l/receive-all (trace/probe-channel "errors:handled") handle-errors)

(l/receive-all (trace/probe-channel "activities:pushed")
               (fn [response]
                 (log/infof "sending update notification to connection: %s"
                            (:connection-id response))
                 (s/increment "activities pushed")))

(l/receive-all (trace/probe-channel "conversations:pushed")
               (fn [response]
                 (log/infof "sending update notification to connection: %s"
                            (:connection-id response))
                 (s/increment "conversations pushed")))
