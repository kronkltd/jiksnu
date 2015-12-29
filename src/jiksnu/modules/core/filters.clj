(ns jiksnu.modules.core.filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions :as actions]
            [slingshot.slingshot :refer [throw+]]))

(defn parse-page
  [request]
  {:page (or (some-> request :params :page Integer/parseInt) 1)})

(defn parse-sorting
  [request]
  (let [order-by (:orderBy (:params request))
        direction (if (= "desc" (:direction (:params request))) -1 1)]
    (when (and order-by direction)
      {:sort-clause {(keyword order-by) direction}})))


(deffilter #'actions/confirm :http
  [action request]
  (let [{:keys [action model id]} (:params request)]
    (action action model id)))
