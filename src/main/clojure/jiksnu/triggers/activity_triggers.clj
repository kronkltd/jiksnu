(ns jiksnu.triggers.activity-triggers
  (:use clj-tigase.core
        ciste.core
        ciste.view
        ciste.trigger
        ciste.sections
        jiksnu.actions.activity-actions
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn notify-activity
  [recipient ^Activity activity]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [recipient-jid (make-jid recipient)
            author (model.user/fetch-by-id (first (:authors activity)))
            message-text (:summary activity)
            ele (make-element
                 ["message" {"type" "headline"}
                  ["event" {"xmlns" event-ns}
                   (index-block [activity])]])
            message
            (make-packet
             {:to recipient-jid
              :from (make-jid author)
              :type :chat
              ;; FIXME: generate an id for this case
              :id "JIKSNU1"
              :body ele})]
        (deliver-packet! message)))))

(defn notify-subscribers
  [action request activity]
  (with-database
    (let [user (model.user/fetch-by-id (first (:authors activity)))
          subscribers (conj (model.subscription/subscribers user) user)]
      (doseq [subscription subscribers]
        (model.item/push user activity)
        (notify-activity user activity)))))

(defn notify-commented
  [_ request activity]
  (let [parent (model.activity/show (:parent activity))]
    (model.activity/add-comment parent activity)))

(defn show-trigger
  [action params activity]
  (println "show trigger")

  )

(add-trigger! #'show #'show-trigger)
(add-trigger! #'create #'notify-commented)
(add-trigger! #'create #'notify-subscribers)
(add-trigger! #'create #'sleep-and-print)
