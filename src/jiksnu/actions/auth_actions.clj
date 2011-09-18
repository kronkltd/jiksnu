(ns jiksnu.actions.auth-actions
  (:use (ciste core
               [debug :only (spy)])
        (jiksnu model session))
  (:require (clojure.tools [logging :as log])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user]))
  (:import javax.security.auth.login.AccountNotFoundException
           javax.security.auth.login.LoginException
           jiksnu.model.Activity
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
      (do (log/error "passwords do not match")
          (throw (LoginException. "passwords do not match"))))
    (do (log/error "user not found")
        (throw (AccountNotFoundException. "user not found")))))

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
