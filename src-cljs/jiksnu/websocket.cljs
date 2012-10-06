(ns jiksnu.websocket
  (:use [jayq.core :only [$ css inner prepend text]]
        [jayq.util :only [clj->js]])
  (:require [clojure.string :as string]
            [goog.events :as events]
            [goog.net.WebSocket :as websocket]
            [goog.net.WebSocket.EventType :as websocket-event]
            [goog.net.WebSocket.MessageEvent :as websocket-message]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.underscore :as _]
            [waltz.state :as state])
  (:use-macros [waltz.macros :only [in out defstate defevent]]))

(def *logger* (log/get-logger "jiksnu.websocket"))

(def default-connection (atom nil))
(def $interface ($ :.connection-info))
(def ws-state (state/machine "websocket state"))
;; (state/set-debug ws-state false)
(state/set ws-state :closed)
(def _view)

(defn set-view
  [view]
  (set! _view view))


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
  [command & [args]]
  (log/info *logger* (format "queuing message: %s(%s)" command args))
  (swap! queued-messages conj [command args])
  (state/set ws-state :queued))

(defn send
  [command & [args]]
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
          (prepend ($ :.activities) body))
        jm))
    (catch js/Error ex
      (log/severe *logger* (str ex)))))


(defn connect
  []
  (state/trigger ws-state :connect))

(defn disconnect
  []
  (state/trigger ws-state :close))

(defn delete-handler
  [event]
  (log/info *logger* "delete callback")
  (let [id (.-id event)]
    (.items _view (_/without (.items _view) id))))


(defn process-event
  [event]
  (when event
    (condp = (.-action event)
      "delete" (delete-handler event)
      (condp = (.-type event)
        "error" (log/severe *logger* (.-message event))
        (log/info *logger* "No match found")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; States
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(doto ws-state
  (defstate :closed
    (in []
        (text $interface "Closed")))

  (defstate :connecting
    (in []
        (text $interface "Connecting")))

  (defstate :idle
    (in []
        (do (text $interface "Connected")
            (if (state/in? ws-state :queued)
              (do
                (log/finer *logger* "processing backlog")
                (let [message (first @queued-messages)]
                  (swap! queued-messages rest)
                  (if (empty? @queued-messages)
                    (state/unset ws-state :queued))
                  (apply send message)))))))

  (defstate :error
    (in [] (text $interface "Error!")))

  (defstate :sending
    (in [] (text $interface "sending")))

  (defstate :receiving
    (in [] (text $interface "sending")))

  (defstate :queued
    (in []
        (log/finer *logger* "queued")
        (text $interface "queued"))
    (out [] (log/info "not queued"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(doto ws-state
  (defevent :connect
    []
    (state/transition ws-state :closed :connecting)
    (let [socket (configure (create))]
      (if-let [socket (open-socket socket)]
        (do (reset! default-connection socket)))))

  (defevent :connected
    []
    (state/transition ws-state :connecting :idle)
    ;; The connection isn't really opened till we send a command
    (send "connect"))

  (defevent :close
    []
    (let [socket @default-connection]
      (. socket (close))
      (state/transition ws-state :idle :closed)))

  (defevent :send
    [m command & args]
    (state/transition ws-state :idle :sending)
    (let [message (str command (when (seq args) (apply str " " args)))]
      (log/info *logger* (format "sending message: %s" message))
      (emit! @default-connection message))
    (state/transition ws-state :sending :idle))

  (defevent :receive
    [m event]
    (if event
      (do (state/transition ws-state :idle :receiving)
          (let [parsed-event (parse-json (.-message event))]
            (process-event parsed-event)
            (state/transition ws-state :receiving :idle)
            parsed-event))
      (log/warn *logger* "undefined event"))))
