(ns jiksnu.filters
  (:use [clojure.core.incubator :only [-?>]]))

(defn parse-page
  [request]
  {:page (or (-?> request :params :page Integer/parseInt) 1)})

(defn parse-sorting
  [request]
  (let [order-by (:orderBy (:params request))
        direction (if (= "desc" (:direction (:params request))) -1 1)]
    (when (and order-by direction)
      {:sort-clause {(keyword order-by) direction}})))


