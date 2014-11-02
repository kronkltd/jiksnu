(ns jiksnu.handlers
  (:require [dommy.core :as dommy]
            [dommy.attrs :refer [attr]]
            ;; dommy.template
            [jiksnu.logging :as jl]
            [jiksnu.model :refer [_model]]
            [jiksnu.viewmodel :as vm]
            [jiksnu.websocket :as ws]
            [lolg :as log])
  (:use-macros [dommy.core :only [sel sel1]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n this self
                                   obj arr def* do*n def*n f*n]]))

(def *logger* (log/get-logger "jiksnu.handlers"))

(defn halt
  [event]
  (.stopPropagation event)
  (.preventDefault event))

(defn add-notification
  [message]
  (.add (.get _model "notifications")
        (obj
         :message message)))

(defn invoke-action
  [e]
  (let [target (.-selectedTarget e)
        action (attr target "data-action")
        ;; action (.data target "action")
        parent (dommy/closest target "*[data-id]")
        model (attr parent "data-model")
        id (attr parent "data-id")
        target-element (sel1 parent "*[data-target]")
        target (when target-element
                 (attr target-element "data-target"))]
    (let [message (str action " >> " model "(" id ")")]
      (ws/send "invoke-action"
               (array model action id target))
      (add-notification message))
    (halt e)))

(defn show-comments
  []
  (.set _model
        "showComments" true))

(defn setup-handlers
  []
  (dommy/listen! [(sel1 :html) "*[data-action]"] :click invoke-action)
  (dommy/listen! [(sel1 :html) "#showComments"] :click show-comments)
  )
