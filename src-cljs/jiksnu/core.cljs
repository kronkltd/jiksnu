(ns jiksnu.core
  (:use [jayq.core :only [$ css inner prepend]])
  (:require [goog.events :as events]
            [goog.dom :as dom]
            ;; [goog.dom.query :as query]
            ;; [goog.net :as net]
            [goog.net.XhrIo :as xhrio]
            [jiksnu.websocket :as ws]))

(def default-connection (atom nil))
(def $interface ($ :#interface))

(defn greet
  [n]
  (str "Hello " n))

(defn set-state
  [state]
  (-> $interface
      (css {:background "red"})
      (inner state)))

(defn find-parent-article
  [element]
  (let [helper (goog.dom.DomHelper.)]
    (.getAncestorByTagNameAndClass helper element "article" "notice")))

(defn position-handler
  [position]
  (let [coords (. position (coords))]
    (console/log "lat: " (. coords (latitude)))
    (console/log "long: " (. coords (longitude)))))

(defn halt
  [event]
  (. event (stopPropagation))
  (. event (preventDefault)))


(defn do-delete-activity
  [x]
  (console/log "Delete button clicked")
  (console/log x)
  (if-let [article (find-parent-article (. x (target)))]
    (let [id (.getAttribute article "id")
          url (str "/notice/" id)]
      (xhrio/send
       url
       (fn [e]
         (console/log e)
         #_(.hide this))
       "DELETE")
      (halt x))
    (console/log "article not found")))

(defn do-like-button
  [x]
  (console/log "like button clicked")

  ;; (halt x)
  )

(defn add-handler
  [handler elements]
  (doseq [i (range (alength elements))]
    (let [element (aget elements i)]
      (events/listen element "click" handler)
      (events/listen element goog.events.EventType/SUBMIT handler))))

(defn do-logout-link
  [event]
  (console/log "Logging out")
  
  (halt event)
  )

(defn ws-opened
  [socket]
  (set-state "opening")
  (fn [event]
    (.debug js/console "Websocket Connection Opened")
    (set-state "connected")
    (ws/emit! socket "connect")))

(defn ws-message
  [jm]
  ;; (.info js/console jm)
  (try
    (let [j (JSON/parse jm)]
      ;; (.debug js/console j)

      (let [body (. j -body)]
        (prepend ($ :.activities) body
                
                ))

      
      )
    (catch js/Error ex
        (.exception js/console ex)
        )

    )
  )

(defn ws-error
  [e]
  (.error js/console "Error")
  (set-state "error"))

(defn ws-closed
  [ev]
  (set-state "closed"))

(defn send-command
  [command & args]
  (ws/emit! @default-connection (apply str command " " args)))

(defn initialize-websockets
  [url]
  (let [socket (ws/create)]
    (if-let [socket (-> socket
                        (ws/configure (ws-opened socket)
                                      ws-message
                                      ws-error
                                      ws-closed
                                      )
                        (ws/connect! url))]
      (do
        (.info js/console "initialized")
        (reset! default-connection socket)))))

(defn main
  []
  (.log js/console "starting application")
  (add-handler do-delete-activity ($ :.delete-button))
  (add-handler do-like-button ($ :.like-button))
  (add-handler do-logout-link ($ :.logout-link))

  ;; (set-loading-indicator)
  (initialize-websockets "ws://renfer.name:8082/websocket"))

(main)
