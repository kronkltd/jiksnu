(ns jiksnu.xmpp.view.activity-view
  (:use [jiksnu.config :only (config)]
        jiksnu.namespace
        jiksnu.xmpp.controller.activity-controller
        jiksnu.xmpp.view
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as activity.model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity))

;; TODO: This should be a special case of full response
;; (defn minimal-response
;;   "Returns a response iq packet containing the ids in entries"
;;   [activities]
;;   (map make-minimal-item activities))

(defsection show-section [Activity :xmpp :xmpp]
  [^Activity activity & options]
  (abdera-to-tigase-element
   (with-format :atom
     (show-section activity))))

(defsection index-line [Activity :xmpp :xmpp]
  [^Activity activity & options]
  (make-element
   "item" {"id" (:_id activity)}
   (show-section activity)))

(defsection index-block [Activity :xmpp :xmpp]
  [activities & options]
  (make-element
   "items" {"node" "urn:xmpp:microblog:0"}
   (map index-line activities)))

(defsection index-section [Activity :xmpp :xmpp]
  [activities & options]
  (make-element
   "pubsub" {}
   (index-block activities)))

(defview #'index :xmpp
  [request activities]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    (index-section activities))
   :to (:from request)
   :from (:to request)
   :type :response})

(defview #'remote-create :xmpp
  [request _]
  nil)

(defview #'fetch-comments :xmpp
  [request activities]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    (index-section activities))
   :to (:from request)
   :from (:to request)
   :type :response})

(defn notify
  [recipient ^Activity activity]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [recipient-jid (make-jid recipient)
            author (model.user/fetch-by-id (first (:authors activity)))
            message-text (:summary activity)
            ele (make-element
                 "message" {"type" "headline"}
                 #_["subject" {} "New activity"]
                 #_["body" {} message-text]
                 ["event" {"xmlns" "http://jabber.org/protocol/pubsub#event"}
                  (index-block [activity])])]
        (let [message
              (make-packet
               {:to recipient-jid
                :from (make-jid author)
                :type :chat
                :body ele})]
          (.initVars message)
          (deliver-packet! message))))))
