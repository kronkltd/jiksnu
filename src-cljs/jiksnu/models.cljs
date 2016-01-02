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
   {:name "activities"}))

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
    {:getActivities (fn [] (this-as item (.fetch subpageService item "activities")))}}))


(def.factory jiksnu.Domains
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "domains"
    :endpoint "domains"
    :deserialize deserializer}))

(def.factory jiksnu.Followings
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "followings"
    :endpoint "followings"
    :deserialize deserializer}))

(def.factory jiksnu.Groups
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "groups"
    :endpoint "groups"
    :deserialize deserializer}))

(def.factory jiksnu.Streams
  [DS]
  (.defineResource
   DS
   #js
   {:name "streams"}))

(def.factory jiksnu.Subscriptions
  [DS]
  (.defineResource
   DS
   #js
   {:name "subscriptions"}))

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
    {:getSubpage   (fn [page-name] (this-as item (.fetch subpageService item page-name)))
     :getFollowers (fn [] (this-as item (.fetch subpageService item "followers")))
     :getFollowing (fn [] (this-as item (.fetch subpageService item "following")))
     :getGroups    (fn [] (this-as item (.fetch subpageService item "groups")))
     :getStreams   (fn [] (this-as item (.fetch subpageService item "streams")))}}))
