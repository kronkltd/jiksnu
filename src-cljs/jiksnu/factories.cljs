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
  [DS]
  (! js/window.DS DS)
  (.defineResource DS
      (obj
       :name "user"
       :endpoint "users"
      :deserialize (fn [resource-name data]
                      (.log js/console "data" data)
                      (if-let [items (.-items (.-data data))]
                        items
                        (.-data data)))
       :methods
       (obj
        :getFollowers (fn getFollowers []
                        (.log js/console "this" (js* "this"))
                        (this-as user
                                 (.log js/console "user" user)))))))

