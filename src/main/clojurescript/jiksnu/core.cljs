(ns jiksnu.core
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.net.XhrIo :as xhrio]))

(defn greet
  [n]
  (str "Hello " n))

(defn find-parent-article
  [element]
  (let [helper (goog.dom.DomHelper.)]
    (.getAncestorByTagNameAndClass helper element "article" "notice")))

(defn do-delete-activity
  [x]
  (console/log "Delete button clicked")
  (console/log x)
  (if-let [article (find-parent-article (.target x))]
    (let [id (.getAttribute article "id")
          url (str "/notice/" id)]
      (goog.net.XhrIo/send
       url
       (fn [e]
         (console/log e)
         #_(.hide this))
       "DELETE")
      (. x (stopPropagation))
      (. x (preventDefault)))
    (console/log "article not found")))

(defn add-handler
  [handler elements]
  (doseq [i (range (alength elements))]
    (let [element (aget elements i)]
      (events/listen element "click" handler)
      (events/listen element goog.events.EventType/SUBMIT handler))))

(defn find-element
  [selector]
  (dom/getElementsByClass "delete-button"))

(defn -main
  []
  (console/log "starting application")
  (add-handler do-delete-activity (find-element "delete-button")))

(-main)
