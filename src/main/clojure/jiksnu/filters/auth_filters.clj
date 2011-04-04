(ns jiksnu.filters.auth-filters
  (:use clojure.contrib.logging
        jiksnu.actions.auth-actions
        ciste.filters
        jiksnu.session
        jiksnu.model)
  (:require [ciste.debug :as debug]
            [jiksnu.model.activity :as activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(deffilter #'guest-login :http
  [action request]
  (let [{{webid "webid"} :params} (debug/spy request)]
    (let [user (model.user/find-or-create-by-uri webid)]
      (debug/spy user))))

(deffilter #'login :http
  [action {{username "username"
     password "password"} :params :as request}]
  (if-let [user (model.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      user
      (error "passwords do not match"))
    (error "user not found")))

(deffilter #'login-page :http
  [action request]
  true)

(deffilter #'logout :http
  [action request]
  true)

(deffilter #'password-page :http
  [action request]
  (let [{{id :pending-id} :session} request]
    (model.user/fetch-by-id id)))
