(ns jiksnu.actions.like-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model.like :as model.like])
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
    :created (time/now)}))

(defn get-likes
  [activity]
  (model.like/fetch-all {:activity (:_id activity)}))

(defaction index
  [& [options & _]]
  (let [page (Integer/parseInt (get options :page "1"))
        page-size 20
        criteria {:sort [{:_id 1}]
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
