(ns jiksnu.triggers.activity-triggers
  (:use (ciste core
               [config :only [config]]
               [debug :only [spy]]
               triggers)
        ciste.sections.default
        jiksnu.actions.activity-actions
        (jiksnu model session view))
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace])
            (jiksnu.actions [comment-actions :as actions.comment]
                            [stream-actions :as actions.stream]
                            [user-actions :as actions.user])
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
    (let [recipient-jid (tigase/make-jid (:username recipient) (:domain recipient))
          author (helpers.activity/get-author activity)
          message-text (:summary activity)
          ele (element/make-element
               ["body" {}
                (str (model.user/get-uri author false) ":  "
                     (:content activity))]
               #_["event" {"xmlns" namespace/event}
                  (index-block [activity])])
          message (tigase/make-packet {:to recipient-jid
                                       :from (tigase/make-jid "updates" (config :domain))
                                       :type :chat
                                       ;; FIXME: generate an id for this case
                                       :id "JIKSNU1"
                                       :body ele})]
      (tigase/deliver-packet! message))))

(defn show-trigger
  [action params activity]
  (println "show trigger"))

(defn fetch-new-comments
  [action params activity]
  (let [author (helpers.activity/get-author activity)
        domain (model.user/get-domain author)]
    (actions.comment/fetch-comments-remote activity)))

(defn fetch-more-comments
  [action params [activity comments]]
  (let [author (helpers.activity/get-author activity)
        domain (model.user/get-domain author)]
    (actions.comment/fetch-comments-remote activity)))

(defn post-trigger
  [action params activity]
  (let [user (helpers.activity/get-author activity)
        subscribers (model.subscription/subscribers user)
        subscriber-users (filter identity
                                 (map (comp model.user/fetch-by-id :from)
                                      subscribers))]
    (model.item/push user activity)
    (when-let [mentioned-uri (:mentioned-uri activity)]
      (log/info (str "parsing link " mentioned-uri)))
    (if-let [parent (model.activity/show (:parent activity))]
      (model.activity/add-comment parent activity))
    (doseq [user subscriber-users]
      (notify-activity user activity))))

(add-trigger! #'actions.comment/fetch-comments #'fetch-more-comments)
(add-trigger! #'create #'post-trigger)
