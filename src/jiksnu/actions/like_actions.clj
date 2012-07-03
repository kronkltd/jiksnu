(ns jiksnu.actions.like-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like])
  (:import jiksnu.model.Like))

(defn admin-index
  [request]
  (model.like/fetch-all {} {:limit 20}))

(defaction like-activity
  [activity user]
  (model.like/create
   {:user (:_id user)
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (time/now)}))

(defn get-likes
  [activity]
  (model.like/fetch-all {:activity (:_id activity)}))

(defaction show
  [tag]
  (implement))

(def index*
  (model/make-indexer 'jiksnu.model.like))

(defaction index
  [& options]
  (apply index* options))

(defaction delete
  [like]
  (model.like/delete like))

(definitializer
  (require-namespaces
   ["jiksnu.filters.like-filters"
    "jiksnu.views.like-views"]))
