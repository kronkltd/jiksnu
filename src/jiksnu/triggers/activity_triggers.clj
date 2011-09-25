(ns jiksnu.triggers.activity-triggers
  (:use (ciste core
               [debug :only (spy)]
               triggers)
        ciste.sections.default
        jiksnu.actions.activity-actions
        (jiksnu model session view))
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (jiksnu [namespace :as namespace])
            (jiksnu.actions [comment-actions :as actions.comment]
                            [stream-actions :as actions.stream])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [item :as model.item]
                          [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn notify-activity
  [recipient ^Activity activity]
  (with-context [:xmpp :xmpp]
    (let [recipient-jid (tigase/make-jid recipient)
          author (get-author activity)
          message-text (:summary activity)
          ele (element/make-element ["event" {"xmlns" namespace/event}
                                     (index-block [activity])])
          message (tigase/make-packet {:to recipient-jid
                                       :from (tigase/make-jid author)
                                       :type :headline
                                       ;; FIXME: generate an id for this case
                                       :id "JIKSNU1"
                                       :body ele})]
      (tigase/deliver-packet! message))))

(defn show-trigger
  [action params activity]
  (println "show trigger"))

(defn fetch-new-comments
  [action params activity]
  (let [author (get-author activity)
        domain (model.domain/show (:domain author))]
    (actions.comment/fetch-comments-remote activity)))

(defn fetch-more-comments
  [action params [activity comments]]
  (let [author (get-author activity)
        domain (model.domain/show (:domain author))]
    (actions.comment/fetch-comments-remote activity)))

(defn post-trigger
  [action params activity]
  (let [user (get-author activity)
        subscribers (model.subscription/subscribers user)
        subscriber-users (map (comp model.user/fetch-by-id :from)
                              subscribers)]
    (model.item/push user activity)
    (if-let [parent (model.activity/show (:parent activity))]
      (model.activity/add-comment parent activity))
    (doseq [user subscriber-users]
      (notify-activity user activity))))

(add-trigger! #'actions.comment/fetch-comments #'fetch-more-comments)
(add-trigger! #'post #'post-trigger)
