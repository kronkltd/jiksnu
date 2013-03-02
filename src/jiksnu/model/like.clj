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

(def set-field! (templates/make-set-field! collection-name))

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  [id]
  (if-let [like (mc/find-map-by-id collection-name id)]
    (model/map->Like like)))

(defn delete
  [like]
  (let [like (fetch-by-id (:_id like))]
    (mc/remove-by-id collection-name (:_id like))
    like))

(def create        (templates/make-create collection-name #'fetch-by-id #'create-validators))

;; TODO: get-like

;; FIXME: This is not quite right
(defn find-or-create
  [activity user]
  (create activity))

(defn fetch-all
  ([] (fetch-all {} {}))
  ([params] (fetch-all params {}))
  ([params opts]
     (map model/map->Like (mc/find-maps collection-name params))))

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

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name)))

;; TODO: deprecated
(defn format-data
  "format a like for display in templates"
  [like]
  (let [user (model.user/fetch-by-id (:user like))]
    (:username user)))
