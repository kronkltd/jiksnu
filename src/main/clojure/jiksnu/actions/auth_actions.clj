(ns jiksnu.actions.auth-actions
  (:use ciste.core
        clojure.contrib.logging
        jiksnu.session
        jiksnu.model)
  (:require [ciste.debug :as debug]
            [jiksnu.model.activity :as activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defaction guest-login
  [webid]
  (let [user (model.user/find-or-create-by-uri webid)]
    user))

(defaction login
  [{{username "username"
     password "password"} :params :as request}]
  (if-let [user (model.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      user
      (error "passwords do not match"))
    (error "user not found")))

(defaction login-page
  [request]
  true)

(defaction logout
  [request]
  true)

(defaction password-page
  [request]
  (let [{{id :pending-id} :session} request]
    (model.user/fetch-by-id id)))
