(ns jiksnu.actions.auth-actions
  (:use clojure.contrib.logging
        jiksnu.session
        jiksnu.model)
  (:require [ciste.debug :as debug]
            [jiksnu.model.activity :as activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn login-page
  [request]
  true)

(defn login
  [{{username "username"
     password "password"} :params :as request}]
  (if-let [user (model.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      user
      (error "passwords do not match"))
    (error "user not found")))

(defn logout
  [request]
  true)

(defn guest-login
  [request]
  (let [{{webid "webid"} :params} (debug/spy request)]
    (let [user (model.user/find-or-create-by-uri webid)]
      (debug/spy user))
    ))

(defn password-page
  [request]
  (let [{{id :pending-id} :session} request]
    (model.user/fetch-by-id id)))
