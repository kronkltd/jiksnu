(ns jiksnu.model
  (:require [jiksnu.ko :as ko])

  (:use-macros [jiksnu.macros :only [defvar]])
  )

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

(defvar AppViewModel
  [this]
  (doto this
    (ko/assoc-observable "statistics")
    (ko/assoc-observable "title")
    (ko/assoc-observable "postForm" (PostForm.))
    (ko/assoc-observable "showPostForm" false)
    (ko/assoc-observable-array "activities")
    (ko/assoc-observable-array "notifications")
    (aset "site" (js-obj))
    (aset "dismissNotification"
          (fn [self]
            (log/info self)
            (.remove (.-notifications this) self))))
  
  (doto (.-site this)
    (ko/assoc-observable "name")))

