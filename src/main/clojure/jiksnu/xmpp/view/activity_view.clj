(ns jiksnu.xmpp.view.activity-view
  (:use [jiksnu.config :only (config)]
        jiksnu.namespace
        jiksnu.xmpp.controller.activity-controller
        jiksnu.xmpp.view
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as activity.model])
  (:import jiksnu.model.Activity))

;; TODO: This should be a special case of full response
;; (defn minimal-response
;;   "Returns a response iq packet containing the ids in entries"
;;   [activities]
;;   (map make-minimal-item activities))

(defmethod show-section Activity
  [^Activity activity]
  (abdera-to-tigase-element
   (atom.view.activity/to-entry activity)))

(defmethod ^Element index-line Activity
  [^Activity activity]
  (make-element
   "item" {"id" (:_id activity)}
   [(show-section activity)]))

(defmethod index-block Activity
  [activities]
  (make-element
   "items" {"node" "urn:xmpp:microblog:0"}
   (map index-line activities)))

(defmethod index-section Activity
  [activities]
  (make-element
   "pubsub" {}
   [(index-block activities)]))

(defview #'index :xmpp
  [request activities]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    [(index-section activities)])
   :to (:from request)
   :from (:to request)
   :type :response})

(defn notify
  [recipient ^Activity activity]
  (let [message
        (make-packet
         {:to recipient
          :from (-> (config) :domain)
          :type :chat
          :body (make-element
                 "message" {}
                 [(make-element
                   "body" {}
                   [(:summary activity)])
                  (index-section [activity])])})]
    (.initVars message)
    (deliver-packet! message)))
