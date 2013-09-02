(ns jiksnu.modules.web.filters.stream-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]]))

(deffilter #'callback-publish :http
  [action request]
  (action (abdera/stream->feed (:body request))))

(deffilter #'direct-message-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/create :http
  [action request]
  (let [params (:params request)]
    (action params)))

(deffilter #'friends-timeline :http
  [action request]
  (-> request :params :id model.user/get-user action))

(deffilter #'group-timeline :http
  [action {{:keys [name]} :params}]
  (action (model.group/fetch-by-name name)))

(deffilter #'home-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'inbox :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'public-timeline :http
  [action request]
  (let [page (or (-?> request :params :page Integer/parseInt) 1)]
    (action {} {:page page})))

(deffilter #'mentions-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))



;; user-timeline

(deffilter #'user-timeline :http
  [action request]
  (let [{{:keys [id username] :as params} :params} request
        acct-id (:* params)]
    (if-let [user (or (and acct-id (model.user/fetch-by-uri acct-id))
                      (and id (model.user/fetch-by-id id))
                      (and username (model.user/get-user username)))]
      (action user))))

