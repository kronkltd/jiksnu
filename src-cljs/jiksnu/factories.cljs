(ns jiksnu.factories
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.factory]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(def.factory jiksnu.Activities
  [DS]
  (.defineResource DS
      (obj
       :name "activities"
       :idAttribute "_id"
       :baseUrl "/api")))

(def.factory jiksnu.Users
  [DS]
  (.defineResource DS
      (obj
       :name "users"
       :idAttribute "_id"
       :baseUrl "/api")))

