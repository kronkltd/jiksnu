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
  (log/info "setting up handlers")
  (.on (js/$ js/document)
       "click" "*[data-action]"
       (fn [e]
         (let [target (js/$ (.-currentTarget e))
               action (.data target "action")
               model (.data target "model")
               parent (.closest target "*[data-id]")
               id (.data parent "id")]
           (let [message (str action " >> " model "(" id ")")]
             (log/info message)
             (ws/send (str "invoke-action " model " " action " " id))
             (.add (.get _model "notifications")
                   (js-obj
                    "message" message)))
           (halt e)))))

