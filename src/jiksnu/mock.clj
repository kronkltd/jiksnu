(ns jiksnu.mock
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.referrant :refer [get-this get-that set-this set-that this that]]
            [jiksnu.session :as session]
            [slingshot.slingshot :refer [throw+]]))

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
        user (actions.user/register
              {:username (fseq :username)
               :password password
               :name (fseq :name)
               :accepted true})
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
        user (actions.user/create (factory :user
                                           {:domain (:_id domain)
                                            :update-source (:_id source)}))]
    (model.user/set-field! user :discovered true)
    (set-that :user user)
    user))

(defn a-stream-exists
  [& [options]]
  (let [user (a-user-exists options)
        params {:user (:_id user)
                :name (fseq :word)}
        stream (actions.stream/create params)]
    (set-that :stream stream)
    stream))

(defn a-feed-source-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (if (:local options)
                     (actions.domain/current-domain)
                     (or (when-not (:local options)
                           (get-this :domain))
                         (a-domain-exists
                          (select-keys options #{:local})))))

        url (fseq :uri)
        source (actions.feed-source/create
                (factory :feed-source
                         {:domain (:_id domain)
                          :topic url
                          :hub (make-uri (:_id domain) "/push/hub")}))]
    (set-this :feed-source source)
    source))

(defn a-record-exists
  [type & [opts]]
  (let [specialized-name (format "a-%s-exists" (name type))
        ;; FIXME: This is reporting a user for some reason
        specialized-var (ns-resolve (the-ns 'jiksnu.mock) (symbol specialized-name))]
    (if specialized-var
      (apply specialized-var opts)
      (let [ns-sym (symbol (format "jiksnu.actions.%s-actions"
                                   (name type)))]
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
                   (a-domain-exists))
        source (or (:update-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists {:domain domain}))
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
        activity (actions.activity/post (factory :activity
                                                 {:update-source (:_id source)}))]
    (set-this :activity activity)
    activity))

(defn there-is-an-activity
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
                              :conversation (:_id conversation)
                              ;; :local true
                              :public (= modifier "public")})))]
    (set-this :activity activity)
    activity))

(defn there-is-an-activity-by-another
  [modifier]
  (let [user (actions.user/create (factory :local-user))]
    (there-is-an-activity {:modifier  modifier
                           :user user})))

(defn a-feed-subscription-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists (select-keys options #{:local})))
        feed-subscription (actions.feed-subscription/create
                           (factory :feed-subscription
                                    {:domain (:_id domain)
                                     :local (:local domain)}))]
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
  (there-is-an-activity {:modifier "public"}))

(defn that-user-posts-activity
  []
  (there-is-an-activity {:modifier "public"
                         :user (get-that :user)}))
