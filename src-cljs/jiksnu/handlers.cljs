(ns jiksnu.handlers
  (:use [jayq.core :only [$ css ]]
        [jiksnu.model :only [_model]])
  (:require [jiksnu.logging :as jl]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws]
            [jayq.core :as jayq]))

(defn halt
  [event]
  (.stopPropagation event)
  (.preventDefault event))

(defn add-notification
  [message]
  (.add (.get _model "notifications")
        (js-obj
         "message" message)))

(defn invoke-action
  [e]
  (let [target (js/$ (.-currentTarget e))
        action (.data target "action")
        parent (.closest target "*[data-id]")
        model (.data parent "model")
        id (.data parent "id")
        target-element (.find parent "*[data-target]")
        target (when target-element
                 (.data target-element "target"))]
    (let [message (str action " >> " model "(" id ")")]
      (ws/send "invoke-action" [ model action id (when target target)])
      (add-notification message))
    (halt e)))


(defn setup-handlers
  []
  (.on (js/$ js/document) "click" "*[data-action]" invoke-action))

