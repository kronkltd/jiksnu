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

(defvar User
  [this]
  (doto this))

(def Activity
  (.extend (.-Model js/Backbone)
           (js-obj
            "defaults" (js-obj
                        "enclosures" nil)
            "initialize" (fn []
                           (log/info "Initialize activity")))))

(def ^{:doc "collection of activities"} Activities
  (.extend (.-Collection js/Backbone)
           (js-obj
            "url" (fn [id] (str "/main/notices/" id ".viewmodel"))
            "model" Activity
            "initialize" (fn [models options]
                           (log/info "init activities")))))

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
     "statistics"   nil
     "title"        nil
     "postForm"     nil
     "showPostForm" true
     "pageInfo"     nil
     "currentUser"  nil
     "notifications" nil
     "items" nil
     "users" nil
     "activities" nil
     "domains" nil
     "groups" nil
     "feedSources" nil
     "followers" nil
     "following" nil
     "subscriptions" nil
     "site" nil
     "dismissNotification"
     (fn [self]
       (.remove (.-notifications this) self))

     "getActivity"
     (fn [id]
       (let [m (.activities this)]
         (aget m id)))

     "getDomain"
     (fn [id]
       (aget (.domains this) id))

     "getFeedSource"
     (fn [id]
       (if-let [source (aget (.feedSources this) id)]
         source
         (log/warn (str "Could not find source: " id))))

     "getGroup"
     (fn [id]
       (aget (.groups this) id))

     "getSubscription"
     (fn [id]
       (aget (.subscriptions this) id))

     "getUser"
     (fn [id]
       (aget (.users this) id))))))
