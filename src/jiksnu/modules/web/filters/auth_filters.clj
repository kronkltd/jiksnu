(ns jiksnu.modules.web.filters.auth-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.authentication-mechanism :as model.auth]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]
            ))

(deffilter #'actions.auth/guest-login :http
  [action request]
  (when-let [uri (-> request :params :webid)]
    (when-let [user (actions.user/find-or-create {:_id uri})]
      (action user))))

(deffilter #'actions.auth/login :http
  [action {{:keys [username password]} :params}]
  (if-let [user (model.user/get-user username)]
    (action user password)
    (throw+ {:type :authentication :message "user not found"})))

(deffilter #'actions.auth/logout :http
  [action request]
  (action))

(deffilter #'actions.auth/password-page :http
  [action request]
  (when-let [id (-?> request :session :pending-id)]
    (when-let [user (model.user/fetch-by-id id)]
      (action user))))

(deffilter #'actions.auth/show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.auth/fetch-by-id id)]
      (action item))))

(deffilter #'actions.auth/verify-credentials :http
  [action request]
  (action))

