(ns jiksnu.filters.auth-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.auth-actions
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]))

(println "auth filters")

(deffilter #'guest-login :http
  [action request]
  (-> request :params :webid actions.user/find-or-create-by-uri action))

(deffilter #'login :http
  [action {{:keys [username password]} :params}]
  (if-let [user (model.user/get-user username)]
    (action user password)
    (throw+ {:type :authentication :message "user not found"})))

(deffilter #'login-page :http
  [action request]
  (action))

(deffilter #'logout :http
  [action request]
  (action))

(deffilter #'password-page :http
  [action request]
  (-> request :session :pending-id model/make-id model.user/fetch-by-id action))

(deffilter #'verify-credentials :http
  [action request]
  (action))
