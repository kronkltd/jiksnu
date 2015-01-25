(ns jiksnu.factories
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.factory]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(def.factory jiksnu.Users
  [DS]
  (.defineResource DS
      (obj
       :name "users"
       :idAttribute "_id"
       :baseUrl "/api"
)
))

