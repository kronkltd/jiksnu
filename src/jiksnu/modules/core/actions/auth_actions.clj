(ns jiksnu.modules.core.actions.auth-actions
  (:require [cemerick.friend.credentials :as creds]
            [jiksnu.modules.core.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

;; TODO: doesn't work yet
(defn guest-login
  [user]
  user)

(defn login
  "Update the current session with this user, if authenticated"
  [username password]
  ;; TODO: Is this an acceptable use of fetch-all?
  (if-let [user (model.user/get-user username)]
    (if-let [mechanisms (seq (model.authentication-mechanism/fetch-all
                              {:user (:_id user)}))]
      (if (->> mechanisms
               (map :value)
               (some (partial creds/bcrypt-verify password)))
        (do
          (timbre/debug "logging in")
          (session/set-authenticated-user! user)
          user)
        (throw+ {:type :authentication :message "passwords do not match" :username username}))
      (throw+ {:type :authentication :message "No authentication mechanisms found" :username username}))
    (throw+ {:type :authentication :message "User not found" :username username})))

(defn password-page
  "Page for when the identifier has come from elsewhere"
  [user]
  user)

(defn verify-credentials
  []
  ;; TODO: actually check
  true)

(defn show
  [mech]
  mech)

(defn whoami
  []
  (session/current-user))

(defn create
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
    (or (when-let [user (model.user/get-user username)]
          (when-let [mechanisms (seq (model.authentication-mechanism/fetch-all
                                      {:user (:_id user)}))]
            (when-let [resp (->> mechanisms
                                 (map :value)
                                 (some (fn [password]
                                         (creds/bcrypt-credential-fn
                                          {username {:username username
                                                     :password password}}
                                          auth-map))))]
              (do (timbre/debugf "Authenticated - %s" username)
                  resp))))
        (do
          (timbre/debugf "Authentication attempt failed - %s" username)
          nil))))
