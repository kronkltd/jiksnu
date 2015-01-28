(ns jiksnu.factories
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.factory]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(def.factory jiksnu.Activities
  [DS]
  (.defineResource DS
                   (obj
                    :name "activities")))

(def.factory jiksnu.Users
  [DS $q subpageService]
  (! js/window.DS DS)
  (.defineResource DS
                   (obj
                    :name "user"
                    :endpoint "users"
                    :deserialize (fn [resource-name data]
                                   (if-let [items (.-items (.-data data))]
                                     items
                                     (.-data data)))
                    :methods
                    (obj
                     :getFollowers
                     (fn getFollowers []
                       (this-as
                        user
                        (.fetch subpageService user "followers")))

                     :getFollowing
                     (fn getFollowing []
                       (this-as
                        user
                        (.fetch subpageService user "following")))))))
