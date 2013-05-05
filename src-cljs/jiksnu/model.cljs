(ns jiksnu.model
  (:use [jayq.util :only [clj->js]])
  (:require [jiksnu.backbone :as backbone]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as jl]
            [lolg :as log])
  (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.model"))

(def
  ^{:doc "The main backbone model"}
  _model)

(def
  ^{:doc "This is the main view model bound to the page"}
  _view)

(def observables   (js-obj))

(def names
  [["activity"                 "activities"               "Activity"]
   ["authentication-mechanism" "authenticationMechanisms" "AuthenticationMechanism" ]
   ["conversation"             "conversations"            "Conversation"]
   ["domain"                   "domains"                  "Domain" ]
   ["feed-source"              "feedSources"              "FeedSource" ]
   ["feed-subscription"        "feedSubscriptions"        "FeedSubscription"]
   ["group"                    "groups"                   "Group" ]
   ["resource"                 "resources"                "Resource"]
   ["subscription"             "subscriptions"            "Subscription"]
   ["user"                     "users"                    "User"]])

(def class-names (map #(nth % 2) names))

(def
  ^{:doc "The list of model names"}
  model-names (map #(nth % 1) names))

(def collection-name
  (->> names
       (map (fn [[k v _]] [k v]))
       (into {})))

(defn load-model
  [model-name id om]
  (log/finer *logger* (format "not loaded: %s(%s)" model-name id))
  (let [coll (.get _model model-name)]
    (.add coll (js-obj "_id" id))
    (let [m (.get coll id)]
      (.fetch m)
      (let [o (.viewModel js/kb m)]
        (aset om id o)
        o))))

(defn init-observable
  [model-name id om m]
  (let [a (.-attributes m)
        o (.observable js/ko a)]
    (log/finer *logger* (format "setting observable (already loaded): %s(%s)" model-name id))
    (aset om id o)
    o))

(defn get-model*
  "Inintialize a new model reference based on the params when a cached ref is not found"
  [model-name id]
  (log/finer *logger* (format "observable not found: %s(%s)" model-name id))
  (if-let [coll (.get _model model-name)]
    (let [om (aget observables model-name)]
      (if-let [m (.get coll id)]
        (init-observable model-name id om m)
        (load-model model-name id om)))
    (log/severe *logger* "could not get collection")))


(defn get-model
  "Given a model name and an id, return an observable representing that model"
  [model-name id]
  (if id
    (if (= (type id) js/String)
      (let [om (aget observables model-name)]
        (if-let [o (aget om id)]
          (do
            (log/finer *logger* (format "cached observable found: %s(%s)" model-name id))
            o)
          (get-model* model-name id)))
      (throw (js/Error. (str id " is not a string"))))
    (log/warn *logger* "id is undefined")))

(defn get-page
  [name]
  (first (.filter (.pages _view)
            (fn [x]
              (if (= (.id x) name)
                x)))))

(defn receive-model
  "Load data into the model store"
  [coll id o data d]
  (let [resp (.add coll data)
        m (.get coll id)
        a (.-attributes m)]
    (o a)))

(defn initializer
  [m coll]
  (this-as this
    (let [n (.-type this)]
      (log/finer *logger* (format "creating %s: %s" n (.stringify js/JSON m))))))

(defvar Statistics
  [this]
  (doto this
    (ko/assoc-observable "activities")
    (ko/assoc-observable "conversations")
    (ko/assoc-observable "domains")
    (ko/assoc-observable "groups")
    (ko/assoc-observable "feedSources")
    (ko/assoc-observable "feedSubscriptions")
    (ko/assoc-observable "subscriptions")
    (ko/assoc-observable "users")))

(defvar PostForm
  [this]
  (doto this
    (ko/assoc-observable "visible" false)
    (ko/assoc-observable "currentPage" "note")))

(def Page
  (.extend
   backbone/Model
   (js-obj
    "type" "Page"
    "defaults" (fn [] (js-obj
                       "page"         1
                       "pageSize"     0
                       "items"        (array)
                       "recordCount"  0
                       "totalRecords" 0))
    "addItem" (fn [id]
                (this-as this
                  (let [a (.get this "items")]
                    (.set this "items" (clj->js (concat [id] a))))))
    "popItem" (fn []
                (this-as this
                  (let [a (.get this "items")
                        i (first a)]
                    (.set this "items" (clj->js (rest a)))
                    i)))
    "hasNext" (fn []
                (this-as this
                  (< (* (.page this)
                        (.pageSize this))
                     (.totalRecords this))))
    "initialize" initializer)))

(def Pages
  (.extend
   backbone/Collection
   (js-obj
    "model" Page
    "type" "Pages"
    "getPage" (fn [name]
                (this-as this
                  (.get this name))))))

(def Notification
  (.extend
   backbone/Model
   (js-obj
    "type" "Notification"
    "dismiss" (fn []
                (this-as this
                  (.remove (.-collection this) this)))
    "default" (js-obj
               "message" ""
               "level"   ""))))

(def Notifications
  (.extend
   backbone/Collection
   (js-obj
    "model" Notification
    "type" "Notifications"

    ;; Add a new notification
    "addNotification"
    (fn [message]
      (this-as this
        (let [notification (Notification.)]
          (.set notification "message" message)
          (.push this notification)))))))


(def Domain
  (.extend
   backbone/Model
   (js-obj
    "type" "Domain"
    "url" (fn [] (this-as this (format "/model/domains/%s.model" (.-id this))))
    "defaults" (js-obj "xmpp"       "unknown"
                       "discovered" nil
                       "links"      (array))
    "idAttribute" "_id"
    "initialize" initializer)))

(def Domains
  (.extend backbone/Collection
           (js-obj
            "type"    "Domains"
            "urlRoot" "/main/domains/"
            "model"   Domain)))

(def Resource
  (.extend backbone/Model
           (js-obj
            "type" "Resource"
            "url" (fn []
                    (this-as this
                      (format "/model/resources/%s.model" (.-id this))))
            "defaults" (js-obj
                        "url"         nil
                        "title"       nil
                        "domain"      nil
                        "status"      nil
                        "contentType" nil
                        "encoding"    nil
                        "location"    nil
                        "created"     nil
                        "properties"  nil
                        "links"       (array)
                        "updated"     nil)
            "idAttribute" "_id"
            "initialize"  initializer)))


(def Resources
  (.extend backbone/Collection
           (js-obj
            "type"    "Resources"
            "model"   Resource)))


(def User
  (.extend backbone/Model
           (js-obj
            "type" "User"
            "url" (fn []
                    (this-as this
                      (format "/model/users/%s.model" (.-id this))))
            "defaults" (js-obj "url"          nil
                               "avatarUrl"    nil
                               "uri"          nil
                               "bio"          nil
                               "username"     nil
                               "location"     ""
                               "domain"       nil
                               "updateSource" nil
                               "links"        (array)
                               "displayName"  nil)
            "idAttribute" "_id"
            "initialize" initializer)))

(def Users
  (.extend backbone/Collection
           (js-obj
            "idAttribute" "_id"
            "type"        "Users"
            "model"       User)))


(def Activity
  (.extend
   backbone/Model
   (js-obj
    "idAttribute" "_id"
    "url" (fn [id]
            (this-as
              this
              (format "/notice/%s.model" (.-id this))))
    "type" "Activity"
    "defaults" (js-obj
                "_id"           ""
                "author"        ""
                "uri"           ""
                "url"           nil
                "links"         (array)
                "source"        "unknown"
                "comments"      (array)
                "resources"     (array)
                "comment-count" 0
                "created"       nil
                "conversation"  nil
                "title"         nil
                "mentioned"     (array)
                "tags"          (array)
                "geo"           nil
                "object"        (js-obj
                                 "object-type" nil)
                "like-count"    0
                "updateSource" nil
                "enclosures"    (array))
    "initialize" initializer)))

(def ^{:doc "collection of activities"}
  Activities
  (.extend backbone/Collection
           (js-obj
            "type" "Activities"
            "urlRoot" "/main/notices/"
            "model" Activity)))

(def Group
  (.extend backbone/Model
           (js-obj
            "defaults" (js-obj
                        "from"     nil
                        "to"       nil
                        "homepage" ""
                        "fullname" ""
                        "nickname" "")
            "url" (fn [id]
                    (this-as
                      this
                      (format "/model/groups/%s.model" (.-id this))))
            "idAttribute" "_id"
            "type" "Group"
            "initialize" initializer)))

(def Groups
  (.extend backbone/Collection
           (js-obj
            "model" Group
            "type" "Groups")))

(def Subscription
  (.extend backbone/Model
           (js-obj
            "type" "Subscription"
            "defaults" (js-obj
                        "from"    nil
                        "to"      nil
                        "created" nil
                        "pending" nil
                        "local"   nil)
            "url" (fn []
                    (this-as this
                      (format "/model/subscriptions/%s.model" (.-id this))))
            "idAttribute" "_id"
            "initialize" initializer)))

(def Subscriptions
  (.extend backbone/Collection
           (js-obj
            "model" Subscription
            "type" "Subscriptions")))

(def FeedSource
  (.extend backbone/Model
           (js-obj
            "type" "FeedSource"
            "url" (fn []
                    (this-as this
                      (format "/model/feed-sources/%s.model" (.-id this))))
            "defaults" (js-obj
                        "callback" nil
                        "created"  nil
                        "updated"  nil
                        "status"   nil
                        "domain"   nil
                        "hub"      nil
                        "mode"     nil
                        "topic"    nil
                        "watchers" (array)
                        "title"    "Feed")
            "idAttribute" "_id"
            "initialize" initializer)))

(def FeedSources
  (.extend backbone/Collection
           (js-obj
            "type" "FeedSources"
            "model" FeedSource)))

(def FeedSubscription
  (.extend backbone/Model
           (js-obj
            "type" "FeedSubscription"
            "url" (fn []
                    (this-as this
                      (format "/model/feedSubscriptions/%s.model" (.-id this))))
            "defaults" (js-obj
                        "domain"   nil
                        "callback" nil
                        "url"      nil)
            "idAttribute" "_id"
            "initialize" initializer)))

(def FeedSubscriptions
  (.extend backbone/Collection
           (js-obj
            "type" "FeedSubscription"
            "model" FeedSubscription)))

(def Conversation
  (.extend backbone/Model
           (js-obj
            "type" "Conversation"
            "defaults" (js-obj
                        "uri"           nil
                        "url"           nil
                        "domain"        nil
                        "update-source" nil
                        "lastUpdated"   nil
                        "created"       nil
                        "activities"    nil
                        "updated"       nil
                        )
            "url" (fn []
                    (this-as this
                      (format "/model/conversations/%s.model" (.-id this))))
            "idAttribute" "_id"
            "initialize" initializer)))

(def Conversations
  (.extend backbone/Collection
           (js-obj
            "type" "Conversations"
            "model" Conversation)))

(def AuthenticationMechanism
  (.extend backbone/Model
           (js-obj
            "type"        "AuthenticationMechanism"
            "idAttribute" "_id"
            "defaults" (js-obj
                        "user" nil
                        "value" nil
                        )
            "url" (fn []
                    (this-as this
                      (format "/model/authenticationMechanisms/%s.model" (.-id this))))
            "initialize"  initializer)))

(def AuthenticationMechanisms
  (.extend backbone/Collection
           (js-obj
            "type" "AuthenticationMechanisms"
            "model" AuthenticationMechanism)))

(def activities    (Activities.))
(def authentication-mechanisms (AuthenticationMechanisms.))
(def conversations  (Conversations.))
(def domains       (Domains.))
(def feed-sources  (FeedSources.))
(def feed-subscriptions (FeedSubscriptions.))
(def groups        (Groups.))
(def notifications (Notifications.))
(def pages         (Pages.))
(def resources     (Resources.))
(def subscriptions (Subscriptions.))
(def users         (Users.))

(def ^{:doc "The main view model for the site"} AppViewModel
  (.extend backbone/Model
           (js-obj
            "defaults"
            (js-obj
             "activities"               activities
             "authenticationMechanisms" authentication-mechanisms
             "conversations"            conversations
             "domains"                  domains
             "currentUser"              nil
             "feedSources"              feed-sources
             "feedSubscriptions"        feed-subscriptions
             "followers"                (array)
             "following"                (array)
             "groups"                   groups
             "pages"                    pages
             "postForm"                 (PostForm.)
             "notifications"            notifications
             "resources"                resources
             "statistics"               nil
             "subscriptions"            subscriptions
             "targetActivity"           nil
             "targetConversation"       nil
             "targetDomain"             nil
             "targetFeedSource"         nil
             "targetGroup"              nil
             "targetResource"           nil
             "targetUser"               nil
             "title"                    nil
             "users"                    users))))
