(ns jiksnu.modules.http.actions
  (:require [cemerick.friend :as friend]
            [ciste.commands :refer [add-command!]]
            [ciste.core :refer [defaction with-format with-serialization]]
            [ciste.event :as event]
            [ciste.filters :refer [filter-action]]
            [ciste.routes :refer [resolve-routes]]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [org.httpkit.server :as server]))

(defonce connections (ref {}))

(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

(defn transform-activities
  [connection-id activity]
  (timbre/with-context {:activity activity}
    (timbre/info "Transforming activity"))
  (json/write-str {:action "model-updated"
                   :connection-id connection-id
                   :type "activity"
                   :id (:_id activity)}))

(defn transform-conversations
  [connection-id item]
  (json/write-str {:action "page-add"
                   :connection-id connection-id
                   :name "public-timeline"
                   :body (:_id item)}))


(defn log-connections
  []
  (doseq [id (keys @connections)]
    (timbre/debugf "%s => %s" id (keys (get @connections id)))))

(defn deregister-connection!
  "Adds a connection to the registry"
  [{:keys [::user-id ::connection-id] :as status}]
  (let [stream (get-in @connections [user-id connection-id])]
    (timbre/debugf "deregistering connection %s(%s) => %s" user-id connection-id stream)

    (dosync
     (alter connections #(update % user-id dissoc connection-id))))
  (log-connections))

(defn register-connection!
  [{:keys [::user-id ::connection-id] :as status} response-channel]
  (dosync
   (alter connections #(assoc-in % [user-id connection-id] response-channel)))
  (log-connections))

(defaction alert-all
  [message]
  (doseq [ch (all-channels)]
    (let [response (json/write-str {:action "add notice" :message message})]
      (s/put! ch response))))

(defn on-receive
  [ch request response-channel body]
  (when-let [resp (actions.stream/handle-command request response-channel body)]
    (server/send! ch resp)))

(defn handle-closed
  [channel status message]
  (let [user-id (::user-id status)
        connection-id (::connection-id status)]
    (timbre/with-context status
      (timbre/debugf "Websocket connection closed. %s => %s" user-id connection-id))
    (deregister-connection! status)))

(defn connect
  [request ch]
  ;; (trace/trace :websocket:connections:established 1)
  (let [user-id (:current friend/*identity*)
        connection-id (util/new-id)
        status {::user-id user-id ::connection-id connection-id}
        response-channel (s/stream* {:description connection-id})]

    (timbre/with-context {:status (prn-str status)}
      (timbre/debugf "Websocket connection opened. %s => %s" user-id connection-id))

    (register-connection! status response-channel)

    (event/notify :connection-opened status)

    (doto ch

      ;; Acknowledge connection
      (server/send! (json/write-str {::connection connection-id}))

      ;; Message handler
      (server/on-receive #(on-receive ch request response-channel %))

      ;; Executes commands for each input
      (server/on-close   #(handle-closed response-channel status %)))

    ;; Send posted activities to connected clients
    (s/connect
     (s/map #(transform-activities connection-id %)
            (bus/subscribe event/events :activity-posted))
     response-channel)

    (s/connect
     (s/map #(transform-conversations connection-id %)
            (bus/subscribe event/events :conversation-created))
     response-channel)

    (s/consume #(server/send! ch %) response-channel)

    #_(s/on-closed ch (partial connection-closed user-id connection-id))

    connection-id))
