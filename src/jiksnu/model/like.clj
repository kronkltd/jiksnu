(ns jiksnu.model.like
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [monger.collection :as mc])
  (:import jiksnu.model.Like))

(def collection-name "likes")

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  [id]
  (model/map->Like (mc/find-map-by-id collection-name id)))

(defn delete
  [like]
  (let [like (fetch-by-id (:_id like))]
    (mc/remove-by-id (:_id like))
    like))

(defn create
  [options]
  (if (:user options)
    (if (:activity options)
      (do
        (log/debug "Creating like")
        (mc/insert collection-name options))
      (throw+ "Must contain an activity"))
    (throw+ "Must contain a user")))

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
