(ns jiksnu.modules.web.filters.stream-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]]))

(deffilter #'actions.stream/callback-publish :http
  [action request]
  (action (abdera/stream->feed (:body request))))

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
  (action))

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
  (let [page (or (-?> request :params :page Integer/parseInt) 1)]
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

