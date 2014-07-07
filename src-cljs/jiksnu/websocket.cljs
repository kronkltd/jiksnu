(ns jiksnu.websocket
  (:require [dommy.core :as dommy]
            [clojure.string :as string]
            [goog.events :as events]
            [goog.net.WebSocket :as websocket]
            [goog.net.WebSocket.EventType :as websocket-event]
            [goog.net.WebSocket.MessageEvent :as websocket-message]
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
  (if (state/in? ws-state :idle)
    (let [message (->> args
                       (map #(.stringify js/JSON (clj->js %)))
                       (string/join " ")
                       (str command " "))]
      (state/trigger ws-state :send message))
    (queue-message command args)))

(defn configure
  "Configures WebSocket"
  [socket]
  (doto socket
    (events/listen websocket-event/OPENED  #(state/trigger ws-state :connected))
    (events/listen websocket-event/CLOSED  #(state/set ws-state :closed))
    (events/listen websocket-event/ERROR   #(state/set ws-state :error))
    (events/listen websocket-event/MESSAGE #(state/trigger ws-state :receive %))))

(defn ws-message
  [jm]
  (try
    (when jm
      (let [jm (js/eval jm)]
        (if-let [body (. jm -body)]
          (dommy/prepend! (sel :.activities) body))
        jm))
    (catch js/Error ex
      (log/severe *logger* (str ex)))))


(defn connect
  []
  (state/trigger ws-state :connect))

(defn disconnect
  []
  (state/trigger ws-state :close))

(defmulti process-event
  (fn [m]
    (if m
      (.-action m))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; States
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(doto ws-state
  (defstate :closed
    (in []
        (dommy/set-text! *interface* "Closed")))

  (defstate :connecting
    (in []
        (dommy/set-text! *interface* "Connecting")))

  (defstate :idle
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
                    (send command args))))))))

  (defstate :error
    (in [] (dommy/set-text! *interface* "Error!")))

  (defstate :sending
    (in [] (dommy/set-text! *interface* "sending")))

  (defstate :receiving
    (in []
        (dommy/set-text! *interface* "receiving")))

  (defstate :queued
    (in []
        (log/finest *logger* "queued")
        (dommy/set-text! *interface* "queued"))
    (out [] (log/finest "not queued"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(doto ws-state
  (defevent :connect
    []
    (state/set-ex ws-state :closed :connecting)
    (let [socket (configure (create))]
      (if-let [socket (open-socket socket)]
        (do (reset! default-connection socket)))))

  (defevent :connected
    []
    (state/set-ex ws-state :connecting :idle)
    ;; The connection isn't really opened till we send a command
    (send "connect" (array)))

  (defevent :close
    []
    (let [socket @default-connection]
      (. socket (close))
      (state/set-ex ws-state :idle :closed)))

  (defevent :send
    [m command & args]
    (state/set-ex ws-state :idle :sending)
    (let [message (str command (when (seq args) (apply str " " args)))]
      (log/fine *logger* (str "sending message: " message))
      (emit! @default-connection message))
    (state/set-ex ws-state :sending :idle))

  (defevent :receive
    [m event]
    (if event
      (do (state/set-ex ws-state :idle :receiving)
          (let [message (.-message event)]
            (let [parsed-event (parse-json message)]
              (when (.isLoggable *logger* goog.debug.Logger.Level.FINE)
                (.debug js/console "Receiving message" parsed-event))
              (process-event parsed-event)
              (state/set-ex ws-state :receiving :idle)
              parsed-event)))
      (do
        #_(.error js/console "undefined event" m event)
        #_(log/warning *logger* "undefined event")
        (.error js/console (.parse js/JSON (.-message m)))))))
