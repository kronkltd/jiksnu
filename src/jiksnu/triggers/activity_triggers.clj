(ns jiksnu.triggers.activity-triggers
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        [ciste.triggers :only [add-trigger!]]
        ciste.sections.default
        jiksnu.actions.activity-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            (clojure.tools [logging :as log])
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
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
  (log/info (str "Sending notice to: " (model.user/get-uri recipient false)))
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
  ;; uri here is a page, potentially containing information about a user
  
  
  (let [mentioned-domain (.getHost (URI. uri))
        link (model/extract-atom-link uri)
        mentioned-user-params (-> link
                                  abdera/fetch-feed
                                  .getAuthor
                                  actions.user/person->user)]
    (actions.user/find-or-create-by-remote-id {:id uri} {})))

(defn create-trigger
  [action params activity]
  (let [author (actions.activity/get-author activity)
        mentioned-users (map #(actions.user/find-or-create-by-remote-id {:id %})
                             (:mentioned-uris activity))
        parent-activities (map #(actions.activity/find-or-create-by-remote-id {:id %})
                               (:irts activity))
        subscribers (map model.subscription/get-actor
                         (model.subscription/subscribers author))
        to-notify (->> (concat subscribers mentioned-users)
                       (filter :local)
                       (into #{}))]
    ;; Add item to author's stream
    (model.item/push author activity)

    ;; Discover conversation
    (doseq [conversation-uri (:conversations activity)]
      (let [atom-link (model/extract-atom-link conversation-uri)
            source (actions.feed-source/find-or-create {:topic atom-link} {})
            ]
        (fetch-remote-feed source)))
    
    ;; Add as a comment to parent posts
    ;; TODO: deprecated
    (if-let [parent (model.activity/show (:parent activity))]
      (model.activity/add-comment parent activity))

    ;; Add as comment to irts
    (doseq [parent parent-activities]
      (model.activity/add-comment parent activity))

    ;; notify users
    (doseq [user to-notify]
      (notify-activity user activity))

    ;; TODO: ping feed subscriptions

    ))

(add-trigger! #'create #'create-trigger)
