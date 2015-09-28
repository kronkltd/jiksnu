(ns jiksnu.modules.http.actions
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
  [connection-id e]
  (let [response {:action "model-updated"
                  :connection-id connection-id
                  :type "activity"
                  :body (:records e)}]
    (bus/publish! ch/events "activities:pushed" response)
    (json/json-str response)))

(defn transform-conversations
  [connection-id e]
  (let [response {:action "page-add"
                  :connection-id connection-id
                  :name "public-timeline"
                  :body (:_id (:records e))}]
    (bus/publish! ch/events "conversations:pushed" response)
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
      (s/put! ch response))))

(defn connect
  [ch]
  ;; (trace/trace :websocket:connections:established 1)
  (let [user-id (:_id (session/current-user))
        connection-id (util/new-id)]


    (let [response-channel (s/stream
                            ;; {
                            ;;  ;; :description (format "Outgoing messages for %s" connection-id)

                            ;;  }

                            )]

      (dosync
       (alter connections #(assoc-in % [user-id connection-id] response-channel)))

      (s/connect
       (s/map (partial transform-activities connection-id)
              ch/posted-activities)
       response-channel)

      (s/connect
       (s/map (partial transform-conversations connection-id)
               ch/posted-conversations)
       response-channel)

      (s/consume
       (partial server/send! ch)
       response-channel))

    #_(s/on-closed ch (partial connection-closed user-id connection-id))

    connection-id))

