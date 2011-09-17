(ns jiksnu.actions.auth-actions
  (:use (ciste core
               [debug :only (spy)])
        (jiksnu model session))
  (:require (clojure.tools [logging :as log])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defaction guest-login
  [webid]
  (actions.user/find-or-create-by-uri webid))

(defaction login
  [username password]
  ;; TODO: fix this
  (if-let [user (model.user/show username)]
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
