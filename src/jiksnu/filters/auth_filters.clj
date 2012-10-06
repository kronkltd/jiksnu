(ns jiksnu.filters.auth-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.auth-actions
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.auth]
            [jiksnu.model.user :as model.user]))

;; guest-login

(deffilter #'guest-login :http
  [action request]
  (-> request :params :webid actions.user/find-or-create-by-uri action))

;; login

(deffilter #'login :http
  [action {{:keys [username password]} :params}]
  (if-let [user (model.user/get-user username)]
    (action user password)
    (throw+ {:type :authentication :message "user not found"})))

(deffilter #'login :command
  [action request]
  (let [[username password] (:args request)
        user (model.user/get-user username)]
    (action user password)))

;; login-page

(deffilter #'login-page :http
  [action request]
  (action))

;; logout

(deffilter #'logout :http
  [action request]
  (action))

;; password-page

(deffilter #'password-page :http
  [action request]
  (-> request :session :pending-id model/make-id model.user/fetch-by-id action))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.auth/fetch-by-id (model/make-id id))]
     (action item))))

;; verify-credentials

(deffilter #'verify-credentials :http
  [action request]
  (action))

;; whoami

(deffilter #'whoami :command
  [action request]
  (action))
