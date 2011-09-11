(ns jiksnu.filters.auth-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        (jiksnu session model)
        jiksnu.actions.auth-actions)
  (:require (clojure.tools [logging :as log])
            (jiksnu.actions [user-actions :as actions.user])))

(deffilter #'guest-login :http
  [action request]
  (let [{{webid :webid} :params} request]
    (actions.user/find-or-create-by-uri webid)))

(deffilter #'login :http
  [action {{username :username
            password :password} :params :as request}]
  (if-let [user (actions.user/show username)]
    ;; TODO: encrypt
    (if (= password (:password user))
      user
      (log/error "passwords do not match"))
    (log/error "user not found")))

(deffilter #'login-page :http
  [action request]
  true)

(deffilter #'logout :http
  [action request]
  true)

(deffilter #'password-page :http
  [action request]
  (let [{{id :pending-id} :session} request]
    (actions.user/fetch-by-id id)))
