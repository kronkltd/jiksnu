(ns jiksnu.handlers
  (:use [jayq.core :only [$ css inner prepend text]]
        [jayq.util :only [map->js]])
  (:require [goog.events :as events]
            [jiksnu.logging :as log]
            [jayq.core :as jayq]))

(defn find-parent-article
  [element]
  #_(let [helper (goog.dom.DomHelper.)]
    (.getAncestorByTagNameAndClass helper element "article" "notice")))

(defn position-handler
  [position]
  (let [coords (. position (coords))]
    (log/info (str "lat: " (. coords (latitude))))
    (log/info (str "long: " (. coords (longitude))))))

(defn halt
  [event]
  (.stopPropagation event)
  (.preventDefault event))

(defn do-delete-activity
  [x]
  (log/info "Delete button clicked")
  (log/info x)
  (if-let [article (find-parent-article (. x (target)))]
    (let [id (.getAttribute article "id")
          url (str "/notice/" id)]
      (xhrio/send
       url
       (fn [e]
         (log/info e)
         #_(.hide this))
       "DELETE")
      (halt x))
    (log/info "article not found")))

(defn do-like-button
  [x]
  (log/info "like button clicked")
  #_(halt x))

(defn add-handler
  [handler elements]
  (doseq [i (range (alength elements))]
    (let [element (aget elements i)]
      (events/listen element "click" handler)
      (events/listen element goog.events.EventType/SUBMIT handler))))

(defn do-logout-link
  [event]
  (console/log "Logging out")
  #_(halt event))

(defn setup-handlers
  []
  (add-handler do-delete-activity ($ :.delete-button))
  (add-handler do-like-button ($ :.like-button))
  (add-handler do-logout-link ($ :.logout-link)))

