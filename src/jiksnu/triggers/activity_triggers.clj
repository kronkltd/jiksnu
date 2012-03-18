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

(defn create-trigger
  [action params activity]
  (let [user (actions.activity/get-author activity)]
    (model.item/push user activity)
    #_(when-let [conversation-uris (:conversation activity)]
        (doseq [conversation-uri conversation-uris]
          (let [atom-link (model/extract-atom-link conversation-uri)]
            (fetch-remote-feed atom-link))))
    (when-let [mentioned-uris (:mentioned-uris activity)]
      (doseq [mentioned-uri mentioned-uris]
        (log/infof "parsing link: %s" mentioned-uri)
        (if-let [mentioned-user (actions.user/find-or-create-by-remote-id {:id mentioned-uri})]
          (do
            mentioned-user
            
            ))))
    (if-let [parent (model.activity/show (:parent activity))]
      (model.activity/add-comment parent activity))
    (when (seq (:irts activity))
      (doseq [id (:irts activity)]
        (try
          (let [parent (actions.activity/find-or-create-by-remote-id id)]
            (model.activity/add-comment parent activity))
          (catch RuntimeException ex
            (log/error ex)))))
    (doseq [user (->> user
                      model.subscription/subscribers
                      (map model.subscription/get-actor)
                      (filter identity))]
      (notify-activity user activity))))

(add-trigger! #'create #'create-trigger)
