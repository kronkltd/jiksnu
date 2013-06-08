(ns jiksnu.events
  (:require [jiksnu.handlers :as handlers]
            [jiksnu.ko :as ko]
            [clojure.string :as string]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws])
  (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.events"))

(defmethod ws/process-event "delete"
  [event]
  (log/info *logger* "delete callback")
  #_(let [id (.-id event)]
    (.items model/_view (_/without (.items _view) id))))

(defmethod ws/process-event "update viewmodel"
  [event]
  (log/fine *logger* "updating viewmodel")
  (vm/process-viewmodel (.-body event)))

(defmethod ws/process-event "error"
  [event]
  (log/severe *logger* (.-message event)))

(defmethod ws/process-event :default
  [event]
  (log/info *logger* (format "No match found: %s" event))
  (jl/info event))

(defmethod ws/process-event "add notice"
  [event]
  (handlers/add-notification (.-message event)))

(defmethod ws/process-event "model-updated"
  [event]
  (log/fine *logger* "model updated")
  (let [data (.-body event)
        id (.-_id data)
        type (.-type event)]
    (model/set-model type id data)))

(defmethod ws/process-event "page-updated"
  [event]
  (log/info *logger* "page updated")
  (let [data (.-body event)
        id (.-id data)
        type (.-type event)]
    (if-let [page (.get model/pages id)]
      (.set page (jl/spy data))
      (log/warning *logger* "Could not find page in collection"))))
