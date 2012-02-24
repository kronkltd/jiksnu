(ns jiksnu.websocket
  (:require [goog.events :as events]
            [goog.net.WebSocket :as websocket]
            [goog.net.WebSocket.EventType :as websocket-event]
            [goog.net.WebSocket.MessageEvent :as websocket-message]))

;; WebSocket
(defn create []
  (goog.net.WebSocket.))

(defn configure
  "Configures WebSocket"
  ([soc opened message]
     (configure soc opened message nil))
  ([soc opened message error]
     (configure soc opened message error nil))
  ([socket opened message error closed]
     (events/listen socket websocket-event/OPENED opened)
     (events/listen socket websocket-event/MESSAGE
                    (fn [ev]
                      (.info js/console ev)
                      (let [payload (. ev -message)
                           [_ cmd body] (re-matches #"/([^ ]+) (.*)" payload)]
                       (.debug js/console "websocket" (str "R: " payload))
                       (message cmd body))))

     (when error
       (events/listen socket websocket-event/ERROR error))

     (when closed
       (events/listen socket websocket-event/CLOSED closed))

     socket))

(defn connect!
  "Connects WebSocket"
  [socket url]
  (try
    (.log js/console (str "connecting to " url))
    (.open socket url)
    socket
    (catch js/Error e
      (.error js/console e)
      (.warn js/console "websocket" "No WebSocket supported, get a decent browser."))))

(defn close!
  "Closes WebSocket"
  [socket]
  (. socket (close)))

(defn emit!
  "Sends a command to server, optionally with message."
  ([socket cmd]
     (emit! socket cmd nil))
  ([socket cmd msg]
     (let [packet (str "/" cmd (when msg (str " " msg)))]
       (.debug js/console "websocket" (str "T: " packet))
       (.send socket packet))))
