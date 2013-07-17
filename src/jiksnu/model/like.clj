(ns jiksnu.model.like
  (:use [jiksnu.transforms :only [set-_id set-created-time set-updated-time]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.result :as result])
  (:import jiksnu.model.Like))

(def collection-name "likes")
(def maker #'model/map->Conversation)
(def default-page-size 20)

(def create-validators
  (validation-set
   (presence-of :_id)
   (presence-of :created)
   (presence-of :updated)
   (presence-of :user)
   (presence-of :activity)))

(defn prepare
  [record]
  (-> record
      set-_id
      set-created-time
      set-updated-time))

(def count-records (templates/make-counter     collection-name))
(def delete        (templates/make-deleter     collection-name))
(def drop!         (templates/make-dropper     collection-name))
(def set-field!    (templates/make-set-field!  collection-name))
(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))
(def create        (templates/make-create      collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn    collection-name maker))

;; TODO: use index to get pagination
(defn fetch-by-user
  [user]
  (fetch-all {:user (:_id user)}))

(defn get-activity
  [like]
  (-> like :activity model.activity/fetch-by-id))

(defn get-actor
  [like]
  (-> like :user model.user/fetch-by-id))

;; TODO: fetch-by-activity
(defn get-likes
  [activity]
  (seq (fetch-all {:activity (:_id activity)})))
