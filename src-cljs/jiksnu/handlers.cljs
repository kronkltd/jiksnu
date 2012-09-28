(ns jiksnu.handlers
  (:use [jayq.core :only [$ css ]]
        [jiksnu.model :only [_model]])
  (:require [jiksnu.logging :as log]
            [jiksnu.websocket :as ws]
            [jayq.core :as jayq]))

(defn halt
  [event]
  (.stopPropagation event)
  (.preventDefault event))

(defn setup-handlers
  []
  (.on (js/$ js/document)
       "click" "*[data-action]"
       (fn [e]
         (let [target (js/$ (.-currentTarget e))
               action (.data target "action")
               parent (.closest target "*[data-id]")
               model (.data parent "model")
               id (.data parent "id")
               target-element (.find parent "*[data-target]")
               target (when target-element
                        (.data target-element "target"))]
           (let [message (str action " >> " model "(" id ")")]
             (log/info message)
             (ws/send (str "invoke-action " model " " action " " id
                           (if target
                             (str " " target))))
             (.add (.get _model "notifications")
                   (js-obj
                    "message" message)))
           (halt e)))))

