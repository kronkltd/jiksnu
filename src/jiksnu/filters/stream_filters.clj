(ns jiksnu.filters.stream-filters
(:use (ciste [debug :only (spy)]
             [filters :only (deffilter)])
      (clojure.core [incubator :only [-?>]])
        jiksnu.actions.stream-actions)
  (:require (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])))


(deffilter #'friends-timeline :http
  [action request]
  (-> request :params :id model.user/show action))

(deffilter #'inbox :http
  [action request]
  (action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'index :xmpp
  [action request]
  (action))

(deffilter #'remote-profile :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))

(deffilter #'remote-user :http
  [action request]
  (-> request :params :*
      actions.user/fetch-by-uri action))

(deffilter #'user-timeline :http
  [action request]
  (let [{{:keys [id username]} :params} request]
    (if-let [user (if id
                    (model.user/fetch-by-id id)
                    (model.user/show username))]
      (action user))))

(deffilter #'user-timeline :xmpp
  [action request]
  (-> request :to
      actions.user/fetch-by-jid action))

(deffilter #'callback-publish :http
  [action request]
  (action request))

