(ns jiksnu.events
  (:require [jiksnu.handlers :as handlers]
            [clojure.string :as string]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws])
  (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.events"))

(defmethod ws/process-event "delete"
  [event]
  (log/info *logger* "delete callback"))

(defmethod ws/process-event "update viewmodel"
  [event]
  (log/fine *logger* "updating viewmodel")
  (vm/process-viewmodel (.-body event)))

(defmethod ws/process-event "error"
  [event]
  (jl/error event))

(defmethod ws/process-event "connect"
  [event]

  )

(defmethod ws/process-event :default
  [event]
  (log/info *logger* (format "No match found: %s" event))
  (jl/info event))

(defmethod ws/process-event "add notice"
  [event]
  (handlers/add-notification (.-message event)))

(defmethod ws/process-event "model-updated"
  [event]
  (log/finest *logger* "model updated")
  (let [data (.-body event)
        id (.-_id data)
        model-name (.-type event)
        m (model/get-model-obj model-name id)]
    (.set m data)
    (.set m (js-obj "loaded" true))))

(defmethod ws/process-event "page-updated"
  [event]
  (log/finest *logger* "page updated")
  (let [data (.-body event)
        id (.-id data)
        type (.-type event)]
    (if-let [page (.get model/pages id)]
      (do (.set page data)
          (.set page "loaded" "true"))
      (log/warning *logger* "Could not find page in collection"))))

(defmethod ws/process-event "sub-page-updated"
  [event]
  (log/finest *logger* "sub page updated")
  (let [data (.-body event)
        page-name (.-id data)
        model-name (.-model event)
        id (.-id event)
        type (.-type event)
        m (model/get-model-obj model-name id)
        coll (.get m "pages")
        page (if-let [page (.get coll page-name)]
               (.set page data)
               (do (.add coll data)
                   (.get coll page-name)))]
    (.set page "loaded" "true")
    page))

(defmethod ws/process-event "page-add"
  [event]
  (let [id (.-body event)
        page-name (.-name event)]
    (if-let [page (.get model/pages page-name)]
      (let [items (.get page "items")]
        (.push items id)
        (.set page "items" items))
      (log/warning *logger* "Could not find page in collection"))))
