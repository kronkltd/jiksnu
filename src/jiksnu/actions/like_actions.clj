(ns jiksnu.actions.like-actions
  (:require [clj-time.core :as time]
            [jiksnu.model.like :as model.like]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]))

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

(defn create
  "create an activity"
  [params]
  (let [item (prepare-create params)]
    (model.like/create item)))

(defn admin-index
  [request]
  (model.like/fetch-all {} {:limit 20}))

(defn delete
  [like]
  (model.like/delete like))

(defn get-likes
  [activity]
  (model.like/fetch-all {:activity (:_id activity)}))

(defn like-activity
  [activity user]
  (create
   {:user (:_id user)
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (time/now)}))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.like))

(defn index
  [& options]
  (apply index* options))

(defn show
  [like]
  like)
