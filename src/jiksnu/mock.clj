(ns jiksnu.mock
  (:require [clj-factory.core :refer [factory fseq]]
            [clj-time.core :as time]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.album-actions :as actions.album]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.referrant :refer [get-this get-that set-this set-that this that]]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(def my-password (ref nil))

(declare a-feed-source-exists)

;; TODO: this part should return the current domain
(defn a-domain-exists
  [& [options]]
  (let [params (factory :domain {:discovered true})]
    (or (model.domain/fetch-by-id (:_id params))
        (let [domain (actions.domain/create params)]
          (set-this :domain domain)
          domain))))

(defn a-remote-domain-exists
  [& [options]]
  (let [params (factory :domain {:_id (fseq :domain)
                                 :discovered true})
        domain (actions.domain/create params)]
    (set-that :domain domain)
    domain))

(defn a-resource-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (a-domain-exists))
        params (factory :resource {:_id (or (:url options)
                                            (make-uri (:_id domain)))})
        resource (actions.resource/create params)]
    (set-this :resource resource)
    resource))

(defn a-user-exists
  [& [opts]]
  (let [password (or (:password opts)
                     (fseq :password))
        username (or (:username opts)
                     (fseq :username))
        user (model.user/get-user username)

        user (or user (actions.user/register
                       {:username username
                        :password password
                        :name (fseq :name)
                        :accepted true}))
        user (if (:admin opts)
               (do (model.user/set-field! user :admin true)
                   (assoc user :admin true))
               user)]
    (set-this :user user)
    (dosync
     (ref-set my-password password))
    user))

(defn a-client-exists
  [& [opts]]
  (let [params (factory :client)]
    (actions.client/create params)))

(defn a-user-exists-with-password
  [password]
  (a-user-exists {} password))

(defn a-remote-user-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-that :domain)
                   (a-domain-exists))
        source (or (:source options)
                   (get-that :feed-source)
                   (a-feed-source-exists {:domain domain}))
        params (factory :user
                        {:domain (:_id domain)
                         :update-source (:_id source)})
        user (or (model.user/fetch-by-id (str "acct:" (:username params) "@" (:domain params)))
                 (actions.user/create params))]
    (model.user/set-field! user :discovered true)
    (set-that :user user)
    user))

(defn a-stream-exists
  [& [{user :user
       stream-name :name
       :as options}]]
  (let [user (or user (a-user-exists options))
        params {:owner (:_id user)
                :name (or stream-name (fseq :word))}
        stream (actions.stream/create params)]
    (set-that :stream stream)
    stream))

(defn a-feed-source-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (if (:local options)
                     (actions.domain/current-domain)
                     (or (when-not (:local options) (get-this :domain))
                         (a-domain-exists options))))

        url (or (:topic options) (fseq :uri))
        source (or (model.feed-source/fetch-by-topic url)
                   (actions.feed-source/create
                    (factory :feed-source
                      {:domain (:_id domain)
                       :topic url
                       :hub (make-uri (:_id domain) "/push/hub")})))]
    (set-this :feed-source source)
    source))

(defn a-record-exists
  [type & [opts]]
  (let [specialized-name (format "a-%s-exists" (name type))
        ;; FIXME: This is reporting a user for some reason
        specialized-var (ns-resolve (the-ns 'jiksnu.mock) (symbol specialized-name))]
    (if specialized-var
      (apply specialized-var opts)
      (let [ns-sym (symbol (format "jiksnu.actions.%s-actions" (name type)))]
        (require ns-sym)
        (if-let [create-fn (ns-resolve (the-ns ns-sym) 'create)]
          (when-let [record (create-fn (factory type opts))]
            (set-this type record)
            record)
          (throw+ (format "could not find %s/create" ns-sym)))))))

(defn a-conversation-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists options))
        source (or (:update-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists (merge options {:domain domain})))
        conversation (actions.conversation/create
                      (factory :conversation {:domain (:_id domain)
                                              :local (:local domain)
                                              :update-source (:_id source)}))]
    (set-this :conversation conversation)
    conversation))

(defn activity-gets-posted
  [& [options]]
  (let [source (or (:feed-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists (select-keys options #{:local})))
        params (factory :activity {:update-source (:_id source)})
        activity (actions.activity/post params)]
    (set-this :activity activity)
    activity))

(defn an-activity-exists
  [& [options]]
  (let [modifier (:modifier options "public")
        user (or (:user options) (get-this :user))
        domain (or (:domain options)
                   (and user (model.domain/fetch-by-id (:domain user)))
                   (get-this :domain)
                   (a-domain-exists (select-keys options #{:local})))
        user (or user (a-user-exists {:domain domain}))
        source (or (:feed-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists {:domain domain}))
        stream (or (:stream options)
                   (get-that :stream)
                   (a-stream-exists {:user user}))
        conversation (or (:conversation options)
                         (a-conversation-exists {:domain domain
                                                 :source source}))
        url (fseq :uri)
        activity (session/with-user user
                   (actions.activity/create
                    (factory :activity
                             {:author (:_id user)
                              :id url
                              :update-source (:_id source)
                              :verb "post"
                              :conversation (:_id conversation)
                              :streams [(:_id stream)]
                              :published (time/now)
                              ;; :local true
                              :public (= modifier "public")})))]
    (set-this :activity activity)
    activity))

(defn a-like-exists
  [& [options]]
  (let [user (or (:user options) (a-user-exists))
        activity (or (:activity options) (an-activity-exists {:user user}))
        params (factory :like {:activity (:_id activity) :user (:_id user)})
        like (actions.like/create params)]
    (set-this :like like)
    like))

(defn there-is-an-activity-by-another
  [modifier]
  (let [user (actions.user/create (factory :local-user))]
    (an-activity-exists {:modifier modifier
                           :user   user})))

(defn an-album-exists
  [& [options]]
  (let [user (or (:user options) (a-user-exists options))
        params (factory :album {:owner (:_id user)})
        album (actions.album/create params)]
    (set-this :album album)
    album))

(defn a-picture-exists
  [& [options]]
  (let [user (a-user-exists)
        activity (an-activity-exists {:user user})
        params {:activity (:_id activity)
                :user (:_id user)}
        picture (actions.picture/create params)]
    (set-this :picture picture)
    picture))

(defn a-feed-subscription-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists (select-keys options #{:local})))
        feed-subscription (actions.feed-subscription/create
                           (factory :feed-subscription
                                    {:domain (:_id domain)
                                     :local (:local domain)})
                           {})]
    (set-this :feed-subscription feed-subscription)
    feed-subscription))

(defn a-subscription-exists
  [& [options]]
  (let [from (or (:from options)
                 (a-user-exists))
        to (or (:to options)
               (a-user-exists))
        item (actions.subscription/create (factory :subscription {:from (:_id from)
                                                                  :to (:_id to)}))]
    (set-this :subscription item)
    item))

(defn a-group-exists
  [& [options]]
  (let [item (actions.group/create (factory :group))]
    (set-this :group item)
    item))

(defn a-request-token-exists
  [& [options]]
  (let [client (or (:client options)
                   (a-client-exists))
        params (factory :request-token {:client (:_id client)})
        item (actions.request-token/create params)]
    (set-this :request-token item)
    item))

(defn this-user-has-a-subscription
  []
  (let [subscription (model.subscription/create
                      (factory :subscription {:actor (:_id (get-this :user))}))]
    (set-this :subscription subscription)
    subscription))

(defn user-has-a-subscription
  []
  (let [subscription (model.subscription/create
                      (factory :subscription {:actor (:_id (get-this :user))}))]
    (set-this :subscription subscription)
    subscription))

(defn user-posts-activity
  []
  (an-activity-exists {:modifier "public"}))

(defn that-user-posts-activity
  []
  (let [user (get-that :user)
        params (factory :activity {:author (:_id user)})]
    (actions.activity/post params)
    #_(an-activity-exists {:modifier "public"
                             :user   user})))

(defn user-has-a-stream
  [& options]
  (let [user (or (:user options) (:_id (get-this :user)))
        params (factory :stream {:owner user})
        stream (actions.stream/create params)]
    (set-this :stream stream)
    stream))

(defn that-user-likes-this-activity
  ([] (that-user-likes-this-activity {}))
  ([options]
   (let [user (:user options (or (get-that :user) (a-user-exists)))
         activity (:activity options (or (get-this :activity) (an-activity-exists)))
         params {:activity (:_id activity)
                 :user (:_id user)}]
     (actions.like/create params))))

(defn a-group-membership-exists
  ([] (a-group-membership-exists {}))
  ([options]
   (let [user (:user options (or (get-that :user) (a-user-exists)))
         group (:group options (or (get-that :group)
                                   (get-this :group)
                                   (a-group-exists)))
         params {:user (:_id user)
                 :group (:_id group)}]
     (actions.group-membership/create params))))
