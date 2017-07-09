(ns jiksnu.modules.core.actions.like-actions
  (:require [clj-time.core :as time]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]))

(def model-ns 'jiksnu.model.like)

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

(def can-delete? (constantly true))

(def delete    (templates.actions/make-delete model.like/delete can-delete?))

(defn get-likes
  [activity]
  (model.like/fetch-all {:activity (:_id activity)}))

(defn like-activity
  [activity user]
  (create
   {:user user
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (time/now)}))

(def index* (templates.actions/make-indexer model-ns))

(defn index
  [& options]
  (apply index* options))

(defn show
  [like]
  like)

(defn fetch-by-activity
  [activity]
  (index {:activity (:_id (:item activity))}))

(defn handle-like-activity
  [activity]
  (let [{:keys [verb]} activity]
    (condp = verb
      "like"
      (let [author (:author activity)]
        (if-let [target (model.activity/fetch-by-id (get-in activity [:object :_id]))]
          (like-activity target author)
          (throw+ {:msg "Could not find target activity"})))
      nil)))
