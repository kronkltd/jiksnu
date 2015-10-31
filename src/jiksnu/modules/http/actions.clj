(ns jiksnu.modules.http.actions
  (:require [ciste.commands :refer [add-command!]]
            [ciste.core :refer [defaction with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [ciste.routes :refer [resolve-routes]]
            [clojure.core.incubator :refer [dissoc-in]]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.channels :as ch]
            [jiksnu.handlers :as handler]
            [jiksnu.predicates :as pred]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [manifold.time :as lt]
            [org.httpkit.server :as server]
            [slingshot.slingshot :refer [throw+ try+]]))

(defonce connections (ref {}))

(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

(defn transform-activities
  [connection-id activity]
  (timbre/with-context {:activity activity}
    (timbre/info "Transforming activity"))
  (json/json-str {:action "model-updated"
                  :connection-id connection-id
                  :type "activity"
                  :id (:_id activity)}))

(defn transform-conversations
  [connection-id item]
  (json/json-str {:action "page-add"
                  :connection-id connection-id
                  :name "public-timeline"
                  :body (:_id item)}))

(defn handle-closed
  [channel status message]
  (let [user-id (:_id (:user status))
        connection-id (:connection status)]
    (timbre/with-context {:user-id user-id :connection-id connection-id}
      (timbre/info "closed connection"))
    (dosync
     (alter connections #(dissoc-in % [user-id connection-id])))))

(defaction alert-all
  [message]
  (doseq [ch (all-channels)]
    (let [response (json/json-str {:action "add notice"
                                   :message message})]
      (s/put! ch response))))

(defn connect
  [request ch]
  ;; (trace/trace :websocket:connections:established 1)
  (let [user-id (:_id (session/current-user))
        connection-id (util/new-id)
        status {:user user-id :connection connection-id}
        response-channel (s/stream)]

    (timbre/with-context {:status (prn-str status)}
      (timbre/info "Websocket connection opened"))

    (dosync
     (alter connections #(assoc-in % [user-id connection-id] response-channel)))

    (bus/publish! ch/events :connection-opened status)

    (server/send! ch (json/json-str {:connection connection-id}))

    ;; Executes commands for each input
    (server/on-receive ch
                       (fn [body]
                         (when-let [resp (actions.stream/handle-command
                                          request
                                          response-channel body)]
                           (server/send! ch resp))))
    (server/on-close ch #(handle-closed response-channel status %))

    (s/connect
     (s/map #(transform-activities connection-id %)
            (bus/subscribe ch/events :activity-posted))
     response-channel)

    (s/connect
     (s/map #(transform-conversations connection-id %)
            (bus/subscribe ch/events :conversation-created))
     response-channel)

    (s/consume #(server/send! ch %) response-channel)

    #_(s/on-closed ch (partial connection-closed user-id connection-id))

    connection-id))
