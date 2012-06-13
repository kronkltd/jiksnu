(ns jiksnu.filters.stream-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.stream-actions)
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]))


(deffilter #'callback-publish :http
  [action request]
  (action request))

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

(deffilter #'remote-profile :http
  [action request]
  (-> request :params :id 
      model.user/fetch-by-id action))

(deffilter #'remote-user :http
  [action request]
  (-> request :params :*
      model.user/fetch-by-uri action))

(deffilter #'user-timeline :http
  [action request]
  (let [{{:keys [id username]} :params} request]
    (if-let [user (if id
                    (model.user/fetch-by-id id)
                    (model.user/get-user username))]
      (action user))))

(deffilter #'user-timeline :xmpp
  [action request]
  (-> request :to
      actions.user/fetch-by-jid action))
