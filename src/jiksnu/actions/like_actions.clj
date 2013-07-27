(ns jiksnu.actions.like-actions
  (:use [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms])
  (:import jiksnu.model.Like))

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

(defaction create
  "create an activity"
  [params]
  (let [item (prepare-create params)]
    (model.like/create item)))

(defn admin-index
  [request]
  (model.like/fetch-all {} {:limit 20}))

(defaction delete
  [like]
  (model.like/delete like))

(defn get-likes
  [activity]
  (model.like/fetch-all {:activity (:_id activity)}))

(defaction like-activity
  [activity user]
  (create
   {:user (:_id user)
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (time/now)}))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.like))

(defaction index
  [& options]
  (apply index* options))

(defaction show
  [like]
  like)
