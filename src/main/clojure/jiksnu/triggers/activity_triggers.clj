(ns jiksnu.triggers.activity-triggers
  (:use ciste.core
        ciste.debug
        ciste.sections.default
        ciste.triggers
        clj-tigase.core
        jiksnu.actions.activity-actions
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
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
            author (get-actor activity)
            message-text (:summary activity)
            ele (make-element
                 ["event" {"xmlns" event-ns}
                  (index-block [activity])])
            message
            (make-packet
             {:to recipient-jid
              :from (make-jid author)
              :type :headline
              ;; FIXME: generate an id for this case
              :id "JIKSNU1"
              :body ele})]
        (deliver-packet! message)))))

(defn show-trigger
  [action params activity]
  (println "show trigger"))

(defn fetch-new-comments
  [action params activity]
  (let [author (get-actor activity)
        domain (model.domain/show (:domain author))]
    (fetch-comments-remote activity)))

(defn fetch-more-comments
  [action params [activity comments]]
  (let [author (get-actor activity)
        domain (model.domain/show (:domain author))]
    (fetch-comments-remote activity)))

(defn post-trigger
  [action params activity]
  (let [user (get-actor activity)
        subscribers (model.subscription/subscribers user)
        subscriber-users (map (comp model.user/fetch-by-id :from)
                              subscribers)]
    (model.item/push user activity)
    (if-let [parent (model.activity/show (:parent activity))]
      (model.activity/add-comment parent activity))
    (doseq [user subscriber-users]
      (notify-activity user activity))))

(add-trigger! #'fetch-comments #'fetch-more-comments)
(add-trigger! #'post #'post-trigger)
