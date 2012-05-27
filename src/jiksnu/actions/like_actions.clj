(ns jiksnu.actions.like-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model.like :as model.like]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Like))

(defn admin-index
  [request]
  (model.like/fetch-all {} :limit 20))

(defaction like-activity
  [activity user]
  (model.like/create
   {:user (:_id user)
    :activity (:_id activity)
    ;; TODO: created flag set lower
    :created (sugar/date)}))

(defn get-likes
  [activity]
  (entity/fetch Like {:activity (:_id activity)}))

(defaction index
  [& [options & _]]
  (let [page (Integer/parseInt (get (spy options) :page "1"))
        page-size 20
        criteria {:sort [(sugar/asc :_id)]
                  :skip (* (dec page) page-size)
                  :limit page-size}
        total-records (model.like/count-records {})
        records (model.like/fetch-all (:where options) criteria)]
    {:items records
     :page page
     :page-size page-size
     :total-records total-records
     :args options}))

(definitializer
  (require-namespaces
   ["jiksnu.filters.like-filters"
    "jiksnu.views.like-views"]))
