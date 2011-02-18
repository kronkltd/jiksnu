(ns jiksnu.http.controller.auth-controller
  (:use clojure.contrib.logging
        jiksnu.session
        jiksnu.model)
  (:require [jiksnu.model.activity :as activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn login
  [{{username "username"
     password "password"} :params :as request}]
  (if-let [user (model.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      username
      (error "passwords do not match"))
    (error "user not found")))

(defn logout
  [request]
  true)
