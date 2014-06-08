(ns jiksnu.events
  (:require [goog.string :as gstring]
            [goog.string.format :as gformat]
            [jiksnu.handlers :as handlers]
            [clojure.string :as string]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws])
  (:use-macros [jiksnu.macros :only [defvar]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n this self
                                   obj arr def* do*n def*n f*n]]))

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
  (log/info *logger* (gstring/format "No match found: %s" event))
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
    (.set m (obj :loaded true))))

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
  (let [data (.-body event)]
    (if-let [page-name (.-id data)]
      (let [model-name (.-model event)
            id (.-id event)
            m (model/get-model-obj model-name id)
            coll (.get m "pages")
            page (if-let [page (.get coll page-name)]
                   (.set page data)
                   (do (.add coll data)
                       (.get coll page-name)))]
        (.set page "loaded" "true")
        page)
      (throw "Page name is undefined"))))

(defmethod ws/process-event "page-add"
  [event]
  (let [id (.-body event)
        page-name (.-name event)]
    (log/fine *logger* (gstring/format "adding page: %s << %s" page-name id))
    (if-let [page (.get model/pages page-name)]
      (let [items (.get page "items")]
        (.set page "items"
              (.union js/_
                      (array id)
                      items)))
      (log/warning *logger* "Could not find page in collection"))))
