(ns jiksnu.model
  (:require [jiksnu.backbone :as backbone]
            [jiksnu.ko :as ko]
            [lolg :as log])
  (:use-macros [jiksnu.macros :only [defvar]]))

(def *logger* (log/get-logger "jiksnu.model"))

(def
  ^{:doc "The list of model names"}
  model-names
  ["activities"
   "domains"
   "groups"
   "feedSources"
   "subscriptions"
   "users"])

(def
  ^{:doc "The main backbone model"}
  _model)

(defn receive-model
  "Load data into the model store"
  [coll id o data d]
  (let [resp (.add coll data)
        m (.get coll id)
        a (.-attributes m)]
    (o a)))

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

(def PageInfo
  (.extend
   backbone/Model
   (js-obj
    "type" "PageInfo"
    "defaults" (js-obj
                "page"         1
                "pageSize"     0
                "items"        []
                "recordCount"  0
                "totalRecords" 0)
    "hasNext" (fn []
                (this-as
                 this
                 (< (* (.page this)
                       (.pageSize this))
                    (.totalRecords this)))))))

(def Notification
  (.extend
   backbone/Model
   (js-obj
    "type" "Notification"
    "dismiss"
    (fn []
      (this-as
       this
       (.remove (.-collection this)
                this)))


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
    "add-notification"
    (fn [message]
      (this-as this
       (let [notification (model/Notification.)]
         (.set notification "message" message)
         (.push this notification)))))))


(defn initializer
  [m coll]
  (this-as this
    (let [n (.-type this)]
      (log/finer *logger* (format "creating %s: %s" n (.stringify js/JSON m))))))

(def Domain
  (.extend
   backbone/Model
   (js-obj
    "type" "Domain"
    "url" (fn [] (this-as this (str "/main/domains/" (.-id this))))
    "defaults" (js-obj "xmpp" "unknown"
                       "links" (array))
    "idAttribute" "_id"
    "initialize" initializer)))

(def Domains
  (.extend backbone/Collection
           (js-obj
            "type" "Domains"
            "urlRoot" "/main/domains/"
            "model" Domain)))


(def User
  (.extend backbone/Model
           (js-obj
            "type" "User"
            "url" (fn []
                    (this-as this
                      (format "/model/users/%s.model" (.-id this))))
            "defaults" (js-obj "url" ""
                               "avatarUrl" nil
                               "uri" ""
                               "bio" ""
                               "username" nil
                               "domain" nil
                               "updateSource" nil
                               "links" (array)
                               "displayName" nil)
            "idAttribute" "_id"
            "initialize" initializer
)))

(def Users
  (.extend backbone/Collection
           (js-obj
            "idAttribute" "_id"
            "type" "Users"
            "model" User)))


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
                "comment-count" 0
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
            "url" (fn [a b c]
                    (this-as this
                      (format "/model/feedSources/%s.model" (.-id this))))
            "defaults" (js-obj
                        "callback" nil
                        "mode" nil
                        "title" nil)
            "idAttribute" "_id"
            "initialize" initializer)))

(def FeedSources
  (.extend backbone/Collection
           (js-obj
            "type" "FeedSources"
            "model" FeedSource)))


(def Conversation
  (.extend backbone/Model
           (js-obj
            "type" "Conversation"
            ;; "defaults" (js-obj
            ;;             )
            "idAttribute" "_id"
            "initialize" initializer)))

(def Conversations
  (.extend backbone/Collection
           (js-obj
            "type" "Conversations"
            "model" Conversation)))

(def activities   (Activities.))
(def conversation (Conversations.))
(def users (Users.))
(def domains (Domains.))
(def subscriptions (Subscriptions.))
(def groups (Groups.))
(def feed-sources (FeedSources.))
(def observables (js-obj))

(def ^{:doc "The main view model for the site"} AppViewModel
  (.extend backbone/Model
   (js-obj
    "defaults"
    (js-obj
     "activities"       activities
     "domains"          domains
     "currentUser"      nil
     "feedSources"      feed-sources
     "followers"        (array)
     "following"        (array)
     "groups"           groups
     "items"            (array)
     "pageInfo"         (PageInfo.)
     "postForm"         (PostForm.)
     "notifications"    (Notifications.)
     "statistics"       nil
     "subscriptions"    subscriptions
     "targetActivity"   nil
     "targetDomain"     nil
     "targetFeedSource" nil
     "targetUser"       nil
     "title"            nil
     "users"            users))))
