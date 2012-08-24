(ns jiksnu.model
  (:require [jiksnu.ko :as ko]
            [jiksnu.logging :as log])
  (:use-macros [jiksnu.macros :only [defvar]]))

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

(defvar Foo
  [this])

(defvar Notification
  [this & [message]]
  (if message
    (ko/assoc-observable this "message" message)
    (ko/assoc-observable this "message")))

(defvar PageInfo
  [this]
  (doto this
    (ko/assoc-observable "page")
    (ko/assoc-observable "pageSize")
    (ko/assoc-observable "recordCount")
    (ko/assoc-observable "totalRecords")

    (aset "hasNext"
          (fn []
            (< (* (.page this)
                  (.pageSize this))
               (.totalRecords this))))))


(def Domain
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "name" "Domain"
            "url" (fn [] (this-as this (str "/main/domains/" (.-id this))))
            "defaults" (js-obj "xmpp" "unknown")
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           (log/debug "init domain")))))

(def Domains
  (.extend (.-Collection js/Backbone)
           (js-obj
            "name" "domains"
            "class" "Domains"
            "urlRoot" "/main/domains/"
            "model" Domain
            "initialize" (fn [models options]
                           (log/debug "init domains")))))


(def User
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "name" "User"
            "class" "User"
            "defaults" (js-obj "url" nil
                               "avatarUrl" nil
                               "uri" ""
                               "bio" ""
                               "links" (array)
                               "displayName" nil)
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           (log/info "init user")))))

(def Users
  (.extend (.-Collection js/Backbone)
           (js-obj
            "idAttribute" "_id"
            "class" "Users"
            "model" User
            "initialize" (fn [models options]
                           (log/info "init users")))))



(def Activity
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "idAttribute" "_id"
            "url" (fn [id] (this-as
                           this
                           (str "/notice/" (.-id this) ".model")))
            "class" "Activity"
            "defaults" (js-obj
                        "_id"        ""
                        "author"     ""
                        "links"      (array)
                        "enclosures" (array))
            ;; "relations" (apply array
            ;;                    [(js-obj
            ;;                      "type"           "HasOne"
            ;;                      "key"            "author"
            ;;                      "relatedModel"   User
            ;;                      "collectionType" Users)])
            "initialize" (fn [model]
                           (log/info "init activity")))))

(def ^{:doc "collection of activities"} Activities
  (.extend (.-Collection js/Backbone)
           (js-obj
            "class" "Activities"
            "urlRoot" "/main/notices/"
            "model" Activity
            "initialize" (fn [models options]
                           (log/info "init activities")))))

(def Group
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "idAttribute" "_id"
            "class" "Group"
            "initialize" (fn [model]
                           (log/info "Initialize group")))))

(def Groups
  (.extend (.-Collection js/Backbone)
           (js-obj
            "model" Group
            "class" "Groups"
            "initialize" (fn [models options]
                           (log/info "init groups")))))

(def Subscription
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "class" "Subscription"
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           (log/info "init subscription")))))

(def Subscriptions
  (.extend (.-Collection js/Backbone)
           (js-obj
            "initialize" (fn [models options]
                           (log/info "init subscriptions")))))

(def FeedSource
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "class" "FeedSource"
            "idAttribute" "_id"
            "initialize" (fn [models options]
                           (log/info "init feed source")))))

(def FeedSources
  (.extend (.-Collection js/Backbone)
           (js-obj
            "initialize" (fn [models options]
                           (log/info "init feed sources")))))





(defvar SiteInfo
  [this]
  (doto this
    (ko/assoc-observable "name")))




(def activities   (Activities.))
(def users (Users.))
(def domains (Domains.))
(def subscriptions (Subscriptions.))
(def groups (Groups.))
(def feed-sources (FeedSources.))
(def observables (js-obj))

(def ^{:doc "The main view model for the site"} AppViewModel
  (.extend
   (.-Model js/Backbone)
   (js-obj
    "defaults"
    (js-obj
     
     "activities"  activities
     "domains"       domains
     "currentUser"   nil

     "dismissNotification" (fn [self]
                             (this-as this
                                      (.remove (.-notifications this) self)))

     "feedSources"   feed-sources
     "followers"     (array)
     "following"     (array)

     "groups"        groups
     "items"         nil
     "pageInfo"      nil
     "postForm"      nil
     "notifications" nil
     "showPostForm"  true
     "site"          nil
     "statistics"    nil
     "subscriptions" subscriptions
     "targetUser"    nil
     "title"         "Jiksnu"
     "users"         users))))
