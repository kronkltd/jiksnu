(ns jiksnu.actions.like-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [ciste.model :as cm]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like])
  (:import jiksnu.model.Like))

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
  (model.like/create
   {:user (:_id user)
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (time/now)}))

(def index*
  (model/make-indexer 'jiksnu.model.like))

(defaction index
  [& options]
  (apply index* options))

(defaction show
  [like]
  like)

(definitializer
  (require-namespaces
   ["jiksnu.filters.like-filters"
    "jiksnu.views.like-views"]))
