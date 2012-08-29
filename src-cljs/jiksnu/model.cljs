(ns jiksnu.model
  (:require [Backbone :as backbone]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log])
  (:use-macros [jiksnu.macros :only [defvar]]))

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
    "class" "PageInfo"
    "defaults" (js-obj
                "page"         1
                "pageSize"     0
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
    "class" "Notification"
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
    "class" "Notifications"

    ;; Add a new notification
    "add-notification"
    (fn [message]
      (this-as this
       (let [notification (model/Notification.)]
         (.set notification "message" message)
         (.push this notification)))))))


(def Domain
  (.extend
   backbone/Model
   (js-obj
    "class" "Domain"
    "url" (fn [] (this-as this (str "/main/domains/" (.-id this))))
    "defaults" (js-obj "xmpp" "unknown"
                       "links" (array))
    "idAttribute" "_id"
    "initialize" (fn [models options]
                   #_(log/debug "init domain")))))

(def Domains
  (.extend backbone/Collection
           (js-obj
            "class" "Domains"
            "urlRoot" "/main/domains/"
            "model" Domain
            "initialize" (fn [models options]
                           #_(log/debug "init domains")))))


(def User
  (.extend backbone/Model
           (js-obj
            "class" "User"
            "defaults" (js-obj "url" nil
                               "avatarUrl" nil
                               "uri" ""
                               "bio" ""
                               "links" (array)
                               "displayName" nil)
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           #_(log/info "init user")))))

(def Users
  (.extend backbone/Collection
           (js-obj
            "idAttribute" "_id"
            "class" "Users"
            "model" User
            "initialize" (fn [models options]
                           #_(log/info "init users")))))


(def Activity
  (.extend
   backbone/Model
   (js-obj
    "idAttribute" "_id"
    "url" (fn [id]
            (this-as
             this
             (format "/notice/%s.model" (.-id this))))
    "class" "Activity"
    "defaults" (js-obj
                "_id"        ""
                "author"     ""
                "url"        nil
                "links"      (array)
                "enclosures" (array))
    "initialize" (fn [model]
                   #_(log/info "init activity")))))

(def ^{:doc "collection of activities"}
  Activities
  (.extend backbone/Collection
           (js-obj
            "class" "Activities"
            "urlRoot" "/main/notices/"
            "model" Activity
            "initialize" (fn [models options]
                           #_(log/info "init activities")
                           ))))

(def Group
  (.extend backbone/Model
           (js-obj
            "idAttribute" "_id"
            "class" "Group"
            "initialize" (fn [model]
                           #_(log/info "Initialize group")))))

(def Groups
  (.extend backbone/Collection
           (js-obj
            "model" Group
            "class" "Groups"
            "initialize" (fn [models options]
                           #_(log/info "init groups")))))

(def Subscription
  (.extend backbone/Model
           (js-obj
            "class" "Subscription"
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           #_(log/info "init subscription")))))

(def Subscriptions
  (.extend backbone/Collection
           (js-obj
            "model" Subscription
            "class" "Subscriptions"
            "initialize" (fn [models options]
                           #_(log/info "init subscriptions")))))

(def FeedSource
  (.extend backbone/Model
           (js-obj
            "class" "FeedSource"
            "defaults" (js-obj
                        "callback" nil
                        "mode" nil
                        )
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           #_(log/info "init feed source")))))

(def FeedSources
  (.extend backbone/Collection
           (js-obj
            "class" "FeedSources"
            "model" FeedSource
            "initialize" (fn [models options]
                           #_(log/info "init feed sources")))))






(def activities   (Activities.))
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
     "targetDomain"     nil
     "targetFeedSource" nil
     "targetUser"       nil
     "title"            nil
     "users"            users))))
