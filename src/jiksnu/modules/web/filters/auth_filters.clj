(ns jiksnu.modules.web.filters.auth-filters
  (:require [ciste.filters :only [deffilter]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.authentication-mechanism :as model.auth]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

;; guest-login

(deffilter #'guest-login :http
  [action request]
  (when-let [uri (-> request :params :webid)]
    (when-let [user (actions.user/find-or-create {:_id uri})]
      (action user))))

;; login

(deffilter #'login :http
  [action {{:keys [username password]} :params}]
  (if-let [user (model.user/get-user username)]
    (action user password)
    (throw+ {:type :authentication :message "user not found"})))

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
  (when-let [id (-?> request :session :pending-id)]
    (when-let [user (model.user/fetch-by-id id)]
      (action user))))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.auth/fetch-by-id id)]
      (action item))))

;; verify-credentials

(deffilter #'verify-credentials :http
  [action request]
  (action))

