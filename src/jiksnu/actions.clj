(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.core :only [defaction with-serialization]]
        [ciste.filters :only [deffilter filter-action]]
        [ciste.routes :only [resolve-routes]]
        [ciste.views :only [defview]]
        [clojure.core.incubator :only [dissoc-in]]
        [clojure.data.json :only [read-json]])
  (:require [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.channels :as ch]
            [jiksnu.session :as session]
            [lamina.core :as l]))

(defaction invoke-action
  [model-name action-name id]
  (try
    (s/increment "actions invoked")
    (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
      (require action-ns)

      (if-let [action (ns-resolve action-ns (symbol action-name))]
        (let [body (with-serialization :command (filter-action action id))]
          {:message "action invoked"
           :model model-name
           :action action-name
           :id id
           :body body})
        (do
          (log/warnf "could not find action for: %s(%s) => %s"
                     model-name id action-name)
          {:message "action not found"
           :action "error"})))
    (catch RuntimeException ex
      (log/error ex)
      (.printStackTrace ex))))

(deffilter #'invoke-action :command
  [action request]
  (apply action (:args request)))

(defview #'invoke-action :json
  [request data]
  {:body data})

(add-command! "invoke-action" #'invoke-action)

(defonce connections (ref {}))

(defn connection-opened
  [connection-id e]
  (log/infof "sending update notification to connection: %s" connection-id)
  (s/increment "activities pushed")
  (json/json-str {:action "model-updated"
                  :type "activity"
                  :body (:records e)}))

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
    (l/siphon
     (l/map* (partial connection-opened connection-id) ch/posted-activities)
     ch)
    (l/on-closed ch (partial connection-closed user-id connection-id))
    connection-id))

(deffilter #'connect :command
  [action request]
  (action (:channel request)))

(defview #'connect :json
  [request response]
  {:body {
          :connection-id response}})

(add-command! "connect" #'connect)

(defn all-channels
  []
  (reduce concat (map vals (vals @connections)))
  )

(defn alert-all
  [message]
  (doseq [ch (all-channels)]
    (l/enqueue ch (json/json-str {:action "add notice"
                                  :message message
                                  }))
    )
  )
