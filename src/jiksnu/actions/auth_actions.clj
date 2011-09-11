(ns jiksnu.actions.auth-actions
  (:use (ciste core
               [debug :only (spy)])
        (jiksnu model session))
  (:require (clojure.tools [logging :as log])
            (jiksnu.actions [user-actions :as actions.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defaction guest-login
  [webid]
  (actions.user/find-or-create-by-uri webid))

(defaction login
  [{{username "username"
     password "password"} :params :as request}]
  (if-let [user (actions.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      user
      (log/error "passwords do not match"))
    (log/error "user not found")))

(defaction login-page
  [request]
  true)

(defaction logout
  [request]
  true)

(defaction password-page
  [request]
  (let [{{id :pending-id} :session} request]
    (actions.user/fetch-by-id id)))
