(ns jiksnu.actions.notification-actions
  (:require [clj-time.core :as time]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.notification :as model.notification]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]))

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
    (model.notification/create item)))

(def can-delete? (constantly true))

(def delete    (templates.actions/make-delete model.notification/delete can-delete?))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.notification))

(defn index
  [& options]
  (apply index* options))

(defn show
  [notification]
  notification)
