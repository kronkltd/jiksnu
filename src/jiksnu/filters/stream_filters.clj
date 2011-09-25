(ns jiksnu.filters.stream-filters
(:use (ciste [debug :only (spy)]
             [filters :only (deffilter)])
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

(deffilter #'user-timeline :http
  [action request]
  (-> request :params :username
      model.user/show
      action))

(deffilter #'index :xmpp
  [action request]
  (action))

(deffilter #'user-timeline :xmpp
  [action request]
  (-> request :to
      actions.user/fetch-by-jid action))

(deffilter #'remote-profile :http
  [action request]
  (let [{{id :id} :params} request]
    (let [user (actions.user/fetch-by-id id)]
      user)))

(deffilter #'remote-user :http
  [action request]
  (let [{{uri :*} :params} request]
    (action (actions.user/fetch-by-uri uri))))

