(ns jiksnu.filters.auth-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        jiksnu.actions.auth-actions)
  (:require (clojure.tools [logging :as log])
            (jiksnu.actions [user-actions :as actions.user])))

(deffilter #'guest-login :http
  [action request]
  (-> request :params :webid action))

(deffilter #'login :http
  [action {{:keys [username password]} :params}]
  (action username password))

(deffilter #'login-page :http
  [action request]
  (action))

(deffilter #'logout :http
  [action request]
  (action))

(deffilter #'password-page :http
  [action request]
  (-> request :session :pending-id action))

(deffilter #'verify-credentials :http
  [action request]
  (action))
