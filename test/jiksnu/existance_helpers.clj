(ns jiksnu.existance-helpers
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.referrant :only [get-this get-that set-this set-that this that]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]))

(def my-password (ref nil))


(declare a-feed-source-exists)


;; TODO: this part should return the current domain
(defn a-domain-exists
  [& [options]]
  (let [params (factory :domain {:discovered true})
        domain (actions.domain/create params)]
    (set-this :domain domain)
    domain))

(defn a-remote-domain-exists
  [& [options]]
  (let [params (factory :domain {:_id (fseq :domain)
                                 :discovered true})
        domain (actions.domain/create params)]
    (set-that :domain domain)
    domain))

(defn a-resource-exists
  [& [options]]
  (let [resource (actions.resource/create (factory :resource))]
    (set-this :resource resource)
    resource))

(defn a-user-exists
  ([] (a-user-exists {:discovered true} "hunter2"))
  ([options]
     (a-user-exists options "hunter2"))
  ([opts password]
     (let [user (actions.user/register
                 {:username (fseq :username)
                  :password password
                  :display-name (fseq :name)
                  :accepted true})
           user (if (:admin opts)
                  (do (model.user/set-field! user :admin true)
                      (assoc user :admin true))
                  user)]
       (set-this :user user)
       (dosync
        (ref-set my-password password))
       user)))

(defn a-user-exists-with-password
  [password]
  (a-user-exists {} password))

(defn a-remote-user-exists
  [& [options]]
  (log/info "another user")
  (let [domain (or (:domain options)
                   (get-that :domain)
                   (a-domain-exists))
        source (or (:source options)
                   (get-that :feed-source)
                   (a-feed-source-exists {:domain domain}))
        user (actions.user/create (factory :user
                                           {:domain (:_id domain)
                                            :update-source (:_id source)}))]
    (model.user/set-field! user :discovered true)
    (set-that :user user)
    user))


(defn activity-gets-posted
  [& [options]]
  (let [source (or (:feed-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists))
        activity (actions.activity/post (factory :activity
                                                 {:update-source source}))]
    (set-this :activity activity)
    activity))

(defn there-is-an-activity
  [& [options]]
  (let [modifier (:modifier options "public")
        user (or (:user options) (get-this :user) (a-user-exists))]
    (let [source (or (:feed-source options)
                     (get-this :feed-source)
                     (a-feed-source-exists))]
      (let [activity (session/with-user user
                       (actions.activity/create
                        (factory :activity
                                 {:author (:_id user)
                                  :update-source (:_id source)
                                  ;; :local true
                                  :public (= modifier "public")})))]
        (set-this :activity activity)
        activity))))

(defn there-is-an-activity-by-another
  [modifier]
  (let [user (actions.user/create (factory :local-user))]
    (there-is-an-activity {:modifier  modifier
                           :user user})))

(defn a-record-exists
  [type & [opts]]
  (let [ns-sym (symbol (format "jiksnu.actions.%s-actions"
                               (name type)))]
    (require ns-sym)
    (if-let [create-fn (ns-resolve (the-ns ns-sym) 'create)]
      (when-let [record (create-fn (factory type opts))]
        (set-this type record)
        record)
      (throw+ (format "could not find %s/create" ns-sym)))))



(defn a-feed-source-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists))
        resource (or (:resource options)
                     (get-this :resource)
                     (a-resource-exists))
        source (actions.feed-source/create
                (factory :feed-source
                         {:resource (:_id resource)
                          :topic (format "http://%s/api/statuses/user_timeline/1.atom" (:_id domain))
                          :hub (format "http://%s/push/hub" (:_id domain))}))]
    (set-this :feed-source source)
    source))


(defn a-feed-subscription-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists))
        feed-subscription (model.feed-subscription/create
                           (factory :feed-subscription
                                    {:domain domain}))]
    (set-this :feed-subscription feed-subscription)
    feed-subscription))



(defn a-subscription-exists
  [& [options]]
  (let [item (model.subscription/create (factory :subscription))]
    (set-this :subscription item)
    item))

(defn a-group-exists
  [& [options]]
  (let [item (actions.group/create (factory :group))]
    (set-this :group item)
    item))

(defn a-conversation-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists))
        source (or (:update-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists))]
    (a-record-exists :conversation {:domain (:_id domain)
                                    :update-source (:_id source)})))


(defn this-user-has-a-subscription
  []
  (let [subscription (model.subscription/create (factory :subscription {:actor (:_id (get-this :user))}))]
    (set-this :subscription subscription)
    subscription))

(defn user-has-a-subscription
  []
  (let [subscription (model.subscription/create (factory :subscription {:actor (:_id (get-this :user))}))]
    (set-this :subscription subscription)
    subscription))

(defn user-posts-activity
  []
  (there-is-an-activity {:modifier "public"}))

(defn that-user-posts-activity
  []
  (there-is-an-activity {:modifier "public"
                         :user (get-that :user)}))

