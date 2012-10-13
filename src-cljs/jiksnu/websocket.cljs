(ns jiksnu.websocket
  (:use [jayq.core :only [$ css inner prepend text]])

  (:require [goog.events :as events]
            [goog.net.WebSocket :as websocket]
            [goog.net.WebSocket.EventType :as websocket-event]
            [goog.net.WebSocket.MessageEvent :as websocket-message]
            [jiksnu.logging :as log]
            [waltz.state :as state])
  (:use-macros [waltz.macros :only [in out defstate defevent]]))

(def default-connection (atom nil))
(def $interface ($ :.connection-info))
(def ws-state (state/machine "websocket state"))
;; (state/set-debug ws-state false)
(state/set ws-state :closed)

;; WebSocket
(defn create []
  (goog.net.WebSocket.))

(defn configure
  "Configures WebSocket"
  [socket]
  (events/listen socket websocket-event/OPENED
                 (fn [socket] (state/set ws-state :idle)
                   (state/trigger ws-state :send "connect")))

  (events/listen socket websocket-event/CLOSED
                 (fn [socket] (state/set ws-state :closed)))


  (events/listen socket websocket-event/ERROR
                 (fn [socket] (state/set ws-state :error)))
  
  (events/listen socket websocket-event/MESSAGE
                 (fn [event] (state/trigger ws-state :receive event)))

  socket)

(defn emit!
  "Sends a command to server, optionally with message."
  ([socket cmd]
     (emit! socket cmd nil))
  ([socket cmd msg]
     (let [packet (str cmd (when msg (str " " msg)))]
       (log/debug packet)
       (.send socket packet))))

(defstate ws-state :closed
  (in [] (text $interface "Closed")))

(defstate ws-state :connecting
  (in [] (text $interface "Connecting")))

(defstate ws-state :idle
  (in [] (text $interface "Connected")))

(defstate ws-state :error
  (in [] (text $interface "Error!")))

(defstate ws-state :sending
  (in [] (text $interface "sending")))

(defstate ws-state :receiving
  (in [] (text $interface "sending")))



(defevent ws-state :connect
  []
  (state/set ws-state :connecting)
  (let [socket (configure (create))
        url js/WEBSOCKET_PATH]
    (if-let [socket (try
                      (.open socket url)
                      socket
                      (catch js/Error e
                        (log/error "No WebSocket supported, get a decent browser.")
                        (state/set ws-state :error)))]
      (reset! default-connection socket))))

(defevent ws-state :close
  []
  (let [socket @default-connection]
    (. socket (close))
    (state/set ws-state :closed)))

(defevent ws-state :send
  [command & args]
  (state/set ws-state :sending)
  (emit! @default-connection (apply str command " " args))
  (state/set ws-state :idle))

(defn send
  [command & args]
  (apply state/trigger ws-state :send command args))

(defn ws-message
  [jm]
  (try
    (when jm
      (let [jm (js/eval jm)]
        (log/debug jm)
        
        (if-let [body (. jm -body)]
          (prepend ($ :.activities) body))
        jm))
    (catch js/Error ex
      (log/error (str ex)))))

(defevent ws-state :receive
  [event]
  (state/set ws-state :receiving)
  (let [parsed-event (.parseJSON js/jQuery (. event -message))]
    (state/set ws-state :idle)
    parsed-event))
