(ns jiksnu.filters.stream-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.stream-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]))


(deffilter #'callback-publish :http
  [action request]
  (action (abdera/stream->feed (:body request))))

(deffilter #'direct-message-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

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

(deffilter #'public-timeline :command
  [action request]
  (action))

(deffilter #'public-timeline :http
  [action request]
  (let [page (or (-?> request :params :page Integer/parseInt) 1)]
    (action {} {:page page})))

(deffilter #'public-timeline :xmpp
  [action request]
  (action))

(deffilter #'mentions-timeline :http
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'user-timeline :http
  [action request]
  (let [{{:keys [id username] :as params} :params} request
        acct-id (:* params)]
    (if-let [user (or (and acct-id (model.user/fetch-by-uri acct-id))
                      (and id (model.user/fetch-by-id (model/make-id id)))
                      (and username (model.user/get-user username)))]
      (action user))))

(deffilter #'user-timeline :xmpp
  [action request]
  (-> request :to
      actions.user/fetch-by-jid action))
