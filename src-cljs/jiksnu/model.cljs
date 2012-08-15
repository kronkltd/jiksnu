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

(def Activity (js-obj))

(def ^{:doc "collection of activities"} Activities
  (.extend (.-Collection js/Backbone)
           (js-obj
            "url" (fn [id] (str "/main/notices/" id ".viewmodel"))
            "model" Activity)))

(defvar SiteInfo
  [this]
  (doto this
    (ko/assoc-observable "name")))

(defvar ^{:doc "The main view model for the site"} AppViewModel
  [this]
  (doto this
    (ko/assoc-observable "statistics")
    (ko/assoc-observable "title")
    (ko/assoc-observable "postForm" (PostForm.))
    (ko/assoc-observable "showPostForm" true)
    (ko/assoc-observable "pageInfo")
    (ko/assoc-observable "currentUser")
    (ko/assoc-observable "targetUser")
    (ko/assoc-observable-array "activities")
    (ko/assoc-observable-array "domains")
    (ko/assoc-observable-array "groups")
    (ko/assoc-observable-array "feedSources")
    (ko/assoc-observable-array "items")
    (ko/assoc-observable-array "notifications")
    (ko/assoc-observable-array "subscriptions")
    (ko/assoc-observable-array "users")
    (ko/assoc-observable "site" (SiteInfo.))

    (aset "dismissNotification"
          (fn [self]
            (.remove (.-notifications this) self)))

    (aset "getActivity"
          (fn [id]
            (let [m (.activities this)]
              (aget m id))))

    (aset "getDomain"
          (fn [id]
            (aget (.domains this) id)))

    (aset "getGroup"
          (fn [id]
            (aget (.groups this) id)))

    (aset "getSubscription"
          (fn [id]
            (aget (.subscriptions this) id)))

    (aset "getUser"
          (fn [id]
            (aget (.users this) id)))

    ))
