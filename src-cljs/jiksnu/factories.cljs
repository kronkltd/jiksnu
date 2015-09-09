(ns jiksnu.factories
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.factory]]
               [purnam.core :only [? ?> ! !> obj arr]]))

;; (def.factory jiksnu.$exceptionHandler
;;   []
;;   (fn [exception cause]
;;     (throw exception)))

(def.factory jiksnu.Activities
  [DS]
  (.defineResource
   DS
   (obj
    :name "activities")))

(def.factory jiksnu.Conversations
  [DS subpageService]
  (.defineResource
   DS
   (obj
    :name "conversations"
    :endpoint "conversations"
    :deserialize (fn [resource-name data]
                   (if-let [items (.-items (.-data data))]
                     items
                     (.-data data)))
    :methods
    (obj
     :getActivities (fn [] (this-as item (.fetch subpageService item "activities")))))))

(def.factory jiksnu.Streams
  [DS]
  (.defineResource
   DS
   (obj
    :name "streams")))

(def.factory jiksnu.Users
  [DS subpageService]
  (! js/window.DS DS)
  (! js/window.suppageService subpageService)
  (.defineResource
   DS
   (obj
    :name "user"
    :endpoint "users"
    :deserialize (fn [resource-name data]
                   (if-let [items (.-items (.-data data))]
                     items
                     (.-data data)))
    :methods
    (obj
     :getFollowers (fn [] (this-as item (.fetch subpageService item "followers")))
     :getFollowing (fn [] (this-as item (.fetch subpageService item "following")))
     :getGroups    (fn [] (this-as item (.fetch subpageService item "groups")))
     :getStreams   (fn [] (this-as item (.fetch subpageService item "streams")))))))
