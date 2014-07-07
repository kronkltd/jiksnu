(ns jiksnu.websocket
  (:require [dommy.core :as dommy]
            [clojure.string :as string]
            [goog.events :as events]
            [goog.net.WebSocket :as websocket]
            [goog.net.WebSocket.EventType :as websocket-event]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.util.underscore :as _]
            [waltz.state :as state])
  (:use-macros [dommy.macros :only [sel sel1]]
               [waltz.macros :only [in out defstate defevent]]))

(def *logger* (log/get-logger "jiksnu.websocket"))

(def default-connection (atom nil))
(def *interface* (sel1 :.connection-info))
(def ws-state (state/machine "websocket state"))

(defn parse-json
  [s]
  (.parseJSON js/jQuery s))

;; WebSocket
(defn create []
  (goog.net.WebSocket.))

(defn emit!
  "Sends a command to server, optionally with message."
  ([socket cmd]
     (.info js/console "emitting without message")
     (emit! socket cmd nil))
  ([socket cmd msg]
     (let [packet (str cmd (when msg (str " " msg)))]
       (.send socket packet))))

(defn open-socket
  [socket]
  (let [url js/WEBSOCKET_PATH]
    (try
      (.open socket url)
      socket
      (catch js/Error e
        (log/severe *logger* "No WebSocket supported, get a decent browser.")
        (state/set ws-state :error)))))

(def queued-messages (atom []))

(defn queue-message
  [command args]
  (log/fine *logger* (str "queuing message: " command " " args))
  (swap! queued-messages conj [command args])
  (state/set ws-state :queued))

(defn send
  [command args]
  (if (state/in? ws-state :connected)
    (let [message (->> args
                       (map #(.stringify js/JSON (clj->js %)))
                       (string/join " "))]
      (log/fine *logger* (str "sending message: " command " " message))
      ;; (.info js/console "command:" command)
      ;; (.info js/console "message:" messgae)
      (emit! @default-connection command message))
    (queue-message command args)))

(defmulti process-event #(.-action %))

(defn receive-message
  [event]
  (if event
    (let [message (.-message event)]
      (log/finer (str "Receiving message: " message))
      (let [parsed-event (parse-json message)]
        (process-event parsed-event)
        parsed-event))
    (.error js/console (.parse js/JSON (.-message m)))))

(defn configure
  "Configures WebSocket"
  [socket]
  (doto socket
    (events/listen websocket-event/OPENED  #(state/trigger ws-state :connected))
    (events/listen websocket-event/CLOSED  #(state/set ws-state :closed))
    (events/listen websocket-event/ERROR   #(state/set ws-state :error))
    (events/listen websocket-event/MESSAGE receive-message)))

(defn connect
  []
  (state/set ws-state :connecting)
  (let [socket (configure (create))]
    (when-let [socket (open-socket socket)]
      (reset! default-connection socket))))

(defn disconnect
  []
  (state/trigger ws-state :close))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; States
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstate ws-state :connected
  (in []
      (do (dommy/set-text! *interface* "Connected")
          (if (state/in? ws-state :queued)
            (do
              (log/finer *logger* "processing backlog")
              (let [message (first @queued-messages)]
                (swap! queued-messages rest)
                (if (empty? @queued-messages)
                  (state/unset ws-state :queued))
                (let [[command args] message]
                  (.info js/console "retrying message:" message)
                  (send command args))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defevent ws-state :connected
  []
  (state/set-ex ws-state :connecting :connected)
  ;; The connection isn't really opened till we send a command
  (send "connect" (array)))

(defevent ws-state :close
  []
  (let [socket @default-connection]
    (state/unset ws-state :connected)
    (.close socket)))
