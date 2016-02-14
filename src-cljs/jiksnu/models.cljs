(ns jiksnu.models
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.factory]]))

(defn deserializer
  [resource-name data]
  (if-let [items (.-items (.-data data))]
    items
    (.-data data)))

;; (def.factory jiksnu.$exceptionHandler
;;   []
;;   (fn [exception cause]
;;     (throw exception)))

(def.factory jiksnu.Activities
  [DS]
  (.defineResource
   DS
   #js
   {:name "activities"
    :methods #js {:getType (constantly "Activity")}}))

(def.factory jiksnu.Conversations
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "conversations"
    :endpoint "conversations"
    :deserialize deserializer
    :methods
    #js
    {:getActivities (fn [] (this-as item (.fetch subpageService item "activities")))
     :getType (constantly "Conversation")}}))


(def.factory jiksnu.Domains
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "domains"
    :endpoint "domains"
    :deserialize deserializer
    :methods #js {:getType (constantly "Domain")}}))

(def.factory jiksnu.Followings
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "followings"
    :endpoint "followings"
    :deserialize deserializer
    :methods #js {:getType (constantly "Following")}}))

(def.factory jiksnu.Groups
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "groups"
    :endpoint "groups"
    :deserialize deserializer
    :methods #js {:getType (constantly "Group")}}))

(def.factory jiksnu.Likes
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "likes"
    :endpoint "likes"
    :deserialize deserializer
    :methods #js {:getType (constantly "Like")}}))

(def.factory jiksnu.Streams
  [DS]
  (.defineResource
   DS
   #js
   {:name "streams"
    :methods #js {:getType (constantly "Stream")}}))

(def.factory jiksnu.Subscriptions
  [DS]
  (.defineResource
   DS
   #js
   {:name "subscriptions"
    :methods #js {:getType (constantly "Subscription")}}))

(def.factory jiksnu.Users
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name        "user"
    :endpoint    "users"
    :deserialize deserializer
    :methods
    #js
    {:getType      (constantly "User")
     :getSubpage   (fn [page-name] (this-as item (.fetch subpageService item page-name)))
     :getFollowers (fn [] (this-as item (.fetch subpageService item "followers")))
     :getFollowing (fn [] (this-as item (.fetch subpageService item "following")))
     :getGroups    (fn [] (this-as item (.fetch subpageService item "groups")))
     :getStreams   (fn [] (this-as item (.fetch subpageService item "streams")))}}))
