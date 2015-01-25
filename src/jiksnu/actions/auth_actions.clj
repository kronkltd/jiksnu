(ns jiksnu.actions.auth-actions
  (:require [cemerick.friend.credentials :as creds]
            [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.transforms :as transforms]
            [noir.util.crypt :as crypt]
            [slingshot.slingshot :refer [throw+]]))

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

;; TODO: doesn't work yet
(defaction guest-login
  [user]
  user)

(defaction login
  [username password]
  ;; TODO: Is this an acceptable use of fetch-all?
  (let [user (model.user/get-user username)]
    (if-let [mechanisms (seq (model.authentication-mechanism/fetch-all
                              {:user (:_id user)}))]
      (if (->> mechanisms
               (log/spy :info)
               (map :value)
               (some (partial crypt/compare password)))
        (do
          (log/debug "logging in")
          (session/set-authenticated-user! user)
          user)
        (throw+ {:type :authentication :message "passwords do not match"}))
      (throw+ {:type :authentication :message "No authentication mechanisms found"}))))

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

(defaction create
  "create an activity"
  [params]
  (let [item (prepare-create params)]
    (model.authentication-mechanism/create item)))

(defn add-password
  "Create a new auth mechanism with the type password that has the crypted password"
  [user password]
  (let [params {:type "password"
                :value (creds/hash-bcrypt password)
                :user (:_id user)}]
    (create params)))

(defn check-credentials
  [auth-map]
  (let [username (:username auth-map)]
    (when-let [user (model.user/get-user username)]
      (when-let [mechanisms (seq (model.authentication-mechanism/fetch-all
                                  {:user (:_id user)}))]
        (->> mechanisms
             (map :value)
             (some (fn [password]
                     (creds/bcrypt-credential-fn
                      {username {:username username
                                 :password password}}
                      auth-map))))))))
