(ns jiksnu.triggers.activity-triggers
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               [triggers :only [add-trigger!]])
        ciste.sections.default
        jiksnu.actions.activity-actions)
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [comment-actions :as actions.comment]
                            [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [item :as model.item]
                          [subscription :as model.subscription]
                          [user :as model.user]))
  (:import java.net.URI
           jiksnu.model.Activity
           jiksnu.model.User))

(defn notify-activity
  [recipient ^Activity activity]
  (let [author (actions.activity/get-author activity)
        ele (element/make-element
             ["body" {}
              (str (model.user/get-uri author false) ":  "
                   (:title activity))])
        message (tigase/make-packet {:to (tigase/make-jid (:username recipient) (:domain recipient))
                                     :from (tigase/make-jid "updates" (config :domain))
                                     :type :chat
                                     ;; FIXME: generate an id for this case
                                     :id "JIKSNU1"
                                     :body ele})]
    (tigase/deliver-packet! message)))

(defn parse-unknown-mention
  "Create a user representing the unknown user uri"
  [uri]
  (let [mentioned-domain (.getHost (URI. uri))
        link (model/extract-atom-link uri)
        mentioned-user-params (-> link
                                  abdera/fetch-feed
                                  .getAuthor
                                  actions.user/person->user)]
    ;; TODO: This couldn't be any more wrong!
    ;; it does the right thing, but it feels wrong.
    (actions.user/find-or-create-by-remote-id
          mentioned-user-params mentioned-user-params)))

(defn post-trigger
  [action params activity]
  (let [user (actions.activity/get-author activity)]
    (model.item/push user activity)
    (when-let [mentioned-uri (:mentioned-uri activity)]
      (log/infof "parsing link: %s" mentioned-uri)
      (if-let [mentioned-user (model.user/fetch-by-remote-id mentioned-uri)]
        (do
          (spy mentioned-user)
          ;; set user id
          )
        (parse-unknown-mention mentioned-uri)))
    (if-let [parent (model.activity/show (:parent activity))]
      (model.activity/add-comment parent activity))
    (doseq [user (->> user
                      model.subscription/subscribers
                      (map (comp model.user/fetch-by-id :from))
                      (filter identity))]
      (notify-activity user activity))))

(add-trigger! #'create #'post-trigger)
