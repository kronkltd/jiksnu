(ns jiksnu.actions.auth-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.model.user :as model.user])
  (:import javax.security.auth.login.AccountNotFoundException
           javax.security.auth.login.LoginException
           org.mindrot.jbcrypt.BCrypt))

(defaction guest-login
  [webid]
  (actions.user/find-or-create-by-uri webid))

(defaction login
  [username password]
  ;; TODO: fix this
  (if-let [user (model.user/get-user username)]
    (if-let [mechanisms (model.authentication-mechanism/fetch-all
                      {:user (:_id user)})]
      (if (some #(BCrypt/checkpw password (:value %))
                mechanisms)
        user
        (do (log/error "passwords do not match")
            (throw (LoginException. "passwords do not match"))))
      (throw (LoginException. "No authentication mechanisms found")))
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
    (model.user/fetch-by-id id)))

(defaction verify-credentials
  []
  true)

(defn add-password
  [user password]
  ;; Create a new authentication mechanism with the type password
  ;; that has the crypted password
  (let [salt (BCrypt/gensalt)]
    (model.authentication-mechanism/create
     {:type "password"
      :value (BCrypt/hashpw password salt)
      :user (:_id user)})))

(definitializer
  (require-namespaces
   ["jiksnu.filters.auth-filters"
    "jiksnu.views.auth-views"]))
