(ns jiksnu.actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.core :only [defaction]]
        [ciste.filters :only [deffilter filter-action]]
        [ciste.routes :only [resolve-routes]]
        [ciste.views :only [defview]]
        [clojure.core.incubator :only [dissoc-in]]
        [clojure.data.json :only [read-json]]
        [jiksnu.session :only [current-user]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [lamina.core :as l]))

(defaction invoke-action
  [model-name action-name id]
  (try
    (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
      (require action-ns)

      (if-let [action (ns-resolve action-ns (symbol action-name))]
        (let [body (filter-action action id)]
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

(defaction connect
  [ch]
  (log/info "connected")
  (let [id (:_id (current-user))
        connection-id (abdera/new-id)]
    (dosync
     (alter connections 
            #(assoc-in % [id connection-id] ch)))
    (l/on-closed ch
                 (fn []
                   (log/info "closed")
                   (dosync
                    (alter connections #(dissoc-in % [id connection-id])))))
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
