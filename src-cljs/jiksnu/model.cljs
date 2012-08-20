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
            "idAttribute" "_id")))

(def Domains
  (.extend (.-Collection js/Backbone)
           (js-obj
            "name" "domains"
            "urlRoot" "/main/domains/"
            "model" Domain
            "initialize" (fn [models options]
                           (log/info "init domains")))))


(def User
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "name" "User"
            "defaults" (js-obj "url" nil
                               "displayName" nil)
            "idAttribute" "_id")))

(def Users
  (.extend (.-Collection js/Backbone)
           (js-obj
            "idAttribute" "_id"
            "model" User)))



(def Activity
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "idAttribute" "_id"
            "url" (fn [id] (this-as
                           this
                           (str "/notice/" (.-id this) ".model")))
            "class" "Activity"
            "defaults" (js-obj
                        "_id"        nil
                        "author"     nil
                        "links"      (array)
                        "enclosures" (array))
            ;; "relations" (apply array
            ;;                    [(js-obj
            ;;                      "type"           "HasOne"
            ;;                      "key"            "author"
            ;;                      "relatedModel"   User
            ;;                      "collectionType" Users)])
            "initialize" (fn [model]
                           (log/info "Initialize activity")))))

(def ^{:doc "collection of activities"} Activities
  (.extend (.-Collection js/Backbone)
           (js-obj
            "class" "Activities"
            "urlRoot" "/main/notices/"
            "model" Activity
            "initialize" (fn [models options]
                           (log/info "init activities"))

            )))








(def Subscription
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "idAttribute" "_id")))

(def FeedSource
  (.extend (.-RelationalModel js/Backbone)
           (js-obj
            "idAttribute" "_id")))







(def FeedSources
  (.extend (.-Collection js/Backbone)
           (js-obj)))

(def Subscriptions
  (.extend (.-Collection js/Backbone)
           (js-obj)))





(defvar SiteInfo
  [this]
  (doto this
    (ko/assoc-observable "name")))






(def ^{:doc "The main view model for the site"} AppViewModel
  (.extend
   (.-Model js/Backbone)
   (js-obj
    "defaults"
    (js-obj
     
     "activities"    (Activities.)
     "domains"       (Domains.)
     "currentUser"   nil

     "dismissNotification" (fn [self]
                             (this-as this
                                      (.remove (.-notifications this) self)))

     "feedSources"   (FeedSources.)
     "followers"     nil
     "following"     nil

     "getActivity" (fn [id]
                     (this-as this
                              (log/info "this")
                              (log/info this)
                              (let [m (.activities this)]
                                (.get m id))))

     "getDomain" (fn [id]
                   (this-as this
                            (aget (.domains this) id)))
     
     "getFeedSource" (fn [id]
                       (this-as this
                                (if-let [source (aget (.feedSources this) id)]
                                  source
                                  (log/warn (str "Could not find source: " id)))))

     "getGroup" (fn [id]
                  (this-as this
                   (aget (.groups this) id)))

     "getSubscription"
     (fn [id]
       (this-as this
                (aget (.subscriptions this) id)))

     "getUser"
     (fn [id]
       (this-as this
                (log/info "this")
                (log/info this)
        (.get (.users this) id)))

     "groups"        nil
     "items"         nil
     "pageInfo"      nil
     "postForm"      nil
     "notifications" nil
     "showPostForm"  true
     "site"          nil
     "statistics"    nil
     "subscriptions" nil
     "targetUser"    nil
     "title"         nil
     "users"         (Users.)))))
