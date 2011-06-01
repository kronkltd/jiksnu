(ns jiksnu.actions.auth-actions
  (:use ciste.core
        jiksnu.model
        jiksnu.session)
  (:require [ciste.debug :as debug]
            [clojure.tools.logging :as logger]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defaction guest-login
  [webid]
  (model.user/find-or-create-by-uri webid))

(defaction login
  [{{username "username"
     password "password"} :params :as request}]
  (if-let [user (model.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      user
      (logger/error "passwords do not match"))
    (logger/error "user not found")))

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
