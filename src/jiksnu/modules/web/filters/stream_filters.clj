(ns jiksnu.modules.web.filters.stream-filters
  (:require [ciste.filters :refer [deffilter]]
            [taoensso.timbre :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+ try+]]))

(deffilter #'actions.stream/direct-message-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/create :http
  [action request]
  (let [params (:params request)]
    (action params)))

(deffilter #'actions.stream/friends-timeline :http
  [action request]
  (-> request :params :id model.user/get-user action))

(deffilter #'actions.stream/group-timeline :http
  [action {{:keys [name]} :params}]
  (action (model.group/fetch-by-name name)))

(deffilter #'actions.stream/home-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/inbox :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/inbox-major :http
  [action request]
  ;; TODO: fetch user
  (if-let [username (get-in request [:params :username])]
    (if-let [user (model.user/get-user username)]
      (action user)
      (throw+ "Could not determine user"))
    (throw+ "Could not determine username")))

(deffilter #'actions.stream/inbox-minor :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/direct-inbox-major :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/direct-inbox-minor :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/public-timeline :http
  [action request]
  (let [page (or (some-> request :params :page Integer/parseInt) 1)]
    (action {} {:page page})))

(deffilter #'actions.stream/mentions-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/user-timeline :http
  [action request]
  (let [{{:keys [id username] :as params} :params} request
        acct-id (:* params)]
    (if-let [user (or (and acct-id (model.user/fetch-by-uri acct-id))
                      (and id (model.user/fetch-by-id id))
                      (and username (model.user/get-user username)))]
      (action user))))
