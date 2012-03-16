(ns jiksnu.actions.auth-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]]
               [model :only [implement]]))
  (:require (clojure.tools [logging :as log])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [authentication-mechanism :as model.authentication-mechanism]
                          [user :as model.user]))
  (:import javax.security.auth.login.AccountNotFoundException
           javax.security.auth.login.LoginException
           org.mindrot.jbcrypt.BCrypt
           ))

(defaction guest-login
  [webid]
  (actions.user/find-or-create-by-uri webid))

(defaction login
  [username password]
  ;; TODO: fix this
  (if-let [user (model.user/get-user username)]
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
    (model.user/fetch-by-id id)))

(defaction verify-credentials
  []
  true)

(defn add-password
  [user password]
  (implement
      ;; Create a new authentication mechanism with the type password
      ;; that has the crypted password
      (let [salt (BCrypt/gensalt)]
        (model.authentication-mechanism/create
         {:type "password"
          :value (BCrypt/hashpw password salt)
          :user (:_id user)
          }
         ))

      )
  )

(definitializer
  (doseq [namespace ['jiksnu.filters.auth-filters
                     'jiksnu.views.auth-views]]
    (require namespace)))
