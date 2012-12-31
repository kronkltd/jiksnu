(ns jiksnu.actions.auth-actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.session :as session]
            [noir.util.crypt :as crypt]))

;; TODO: doesn't work yet
(defaction guest-login
  [user]
  user)

(defaction login
  [user password]
  ;; TODO: Is this an acceptable use of fetch-all?
  (if-let [mechanisms (seq (model.authentication-mechanism/fetch-all
                            {:user (:_id user)}))]
    (if (->> mechanisms
             (map :value)
             (some (partial crypt/compare password)))
      (session/set-authenticated-user! user)
      (throw+ {:type :authentication :message "passwords do not match"}))
    (throw+ {:type :authentication :message "No authentication mechanisms found"})))

(add-command! "auth" #'login)

(defaction login-page
  [request]
  ;; TODO: Should this display the login page if already logged in?
  true)

(defaction logout
  [request]
  ;; TODO: close any open session resources, send away presence?
  true)

(defaction password-page
  "Page for when the identifier has come from elsewhere"
  [user]
  user)

(defaction verify-credentials
  []
  ;; TODO: actually check
  true)

(defaction show
  [mech]
  mech)

(defaction whoami
  []
  (session/current-user))

(add-command! "whoami" #'whoami)

(defn add-password
  "Create a new auth mechanism with the type password that has the crypted password"
  [user password]
  (model.authentication-mechanism/create
   {:type "password"
    :value (crypt/encrypt password)
    :user (:_id user)}))

(definitializer
  (require-namespaces
   ["jiksnu.filters.auth-filters"
    "jiksnu.views.auth-views"]))
