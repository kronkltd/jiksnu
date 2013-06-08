(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [*environment*]]
        [ciste.core :only [defaction with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.routes :only [resolve-routes]]
        [ciste.sections.default :only [link-to]]
        [clojure.core.incubator :only [dissoc-in]]
        [clojure.data.json :only [read-json]]
        [slingshot.slingshot :only [try+]])
  (:require #_[clj-airbrake.core :as airbrake]
            [ciste.predicates :as pred]
            [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clout.core :as clout]
            [compojure.core :as compojure]
            [jiksnu.abdera :as abdera]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [lamina.core :as l]
            [lamina.trace :as trace])
  (:import clojure.lang.ExceptionInfo))


(defn name-path-matches?
  [request matcher]
  (if-let [path (:name matcher)]
    (let [request (assoc request :uri (:name request))
          pattern (clout/route-compile path)]
      (if-let [route-params (clout/route-matches pattern request)]
        (#'compojure/assoc-route-params request route-params)))))

(defonce connections (ref {}))

(defonce
  ^{:dynamic true
    :doc "The sequence of predicates used for command dispatch.
          By default, commands are dispatched by name."}
  *page-predicates*
  (ref [#'name-path-matches?]))

(defonce
  ^{:dynamic true}
  *page-matchers*
  (ref []))

(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

(defn alert-all
  [message]
  (doseq [ch (all-channels)]
    (let [response (json/json-str {:action "add notice"
                                   :message message})]
      (l/enqueue ch response))))

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

(declare get-model)

(defaction confirm
  [action model id]
  (when-let [item (get-model model id)]
    {:item item
     :action action}))

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

(defaction get-model
  [model-name id]
  (let [model-ns (symbol (str "jiksnu.model." model-name))]
    (require model-ns)
    (let [fetcher (ns-resolve model-ns 'fetch-by-id)]
      (log/debugf "getting model %s(%s)" model-name id)
      (fetcher id))))

(defaction get-page
  [page-name & args]
  (log/infof "Getting page: %s" page-name)
  (let [request {:format :json
                 :serialization :page
                 :name page-name
                 :args args}]
    ((resolve-routes @*page-predicates* @*page-matchers*) request)))

(defaction invoke-action
  [model-name action-name id]
  (try+
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
      (log/spy :info &throw-context)
      (trace/trace "errors:handled" ex))))

(add-command! "invoke-action" #'invoke-action)
(add-command! "connect" #'connect)
(add-command! "get-model" #'get-model)
(add-command! "get-page" #'get-page)

(defn set-handlers
  []
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
                   (s/increment "conversations pushed"))))

(set-handlers)
