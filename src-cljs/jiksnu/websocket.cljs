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

(defn parse-json
  [s]
  (.parseJSON js/jQuery s))

;; WebSocket
(defn create []
  (goog.net.WebSocket.))

(defn emit!
  "Sends a command to server, optionally with message."
  ([socket cmd]
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
        (log/error "No WebSocket supported, get a decent browser.")
        (state/set ws-state :error)))))

(defn send
  [& commands]
  (state/trigger ws-state :send (apply str commands)))

(defn configure
  "Configures WebSocket"
  [socket]
  (events/listen socket websocket-event/OPENED
                 (fn [socket] (state/trigger ws-state :connected)))

  (events/listen socket websocket-event/CLOSED
                 (fn [socket] (state/set ws-state :closed)))

  (events/listen socket websocket-event/ERROR
                 (fn [socket] (state/set ws-state :error)))
  
  (events/listen socket websocket-event/MESSAGE
                 (fn [event] (state/trigger ws-state :receive event)))

  socket)

(defn ws-message
  [jm]
  (try
    (when jm
      (let [jm (js/eval jm)]
        (if-let [body (. jm -body)]
          (prepend ($ :.activities) body))
        jm))
    (catch js/Error ex
      (log/error (str ex)))))


(defn connect
  []
  (state/trigger ws-state :connect))

(defn disconnect
  []
  (state/trigger ws-state :close))

(defn process-event
  [event]
  (let [t (:type (js->clj event))]
    (log/info t)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; States
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defevent ws-state :connect
  []
  (state/transition ws-state :closed :connecting)
  (let [socket (configure (create))]
    (if-let [socket (open-socket socket)]
      (do (reset! default-connection socket)))))

(defevent ws-state :connected
  []
  (state/transition ws-state :connecting :idle)
  ;; The connection isn't really opened till we send a command
  (send "connect"))

(defevent ws-state :close
  []
  (let [socket @default-connection]
    (. socket (close))
    (state/transition ws-state :idle :closed)))

(defevent ws-state :send
  [m command & args]
  (state/transition ws-state :idle :sending)
  (let [message (str command (when (seq args) (apply str " " args)))]
    (emit! @default-connection message))
  (state/transition ws-state :sending :idle))

(defevent ws-state :receive
  [m event]
  (do (state/transition ws-state :idle :receiving)
      (let [parsed-event (parse-json (. event -message))]
        (process-event parsed-event)
        (state/transition ws-state :receiving :idle)
        parsed-event)))
