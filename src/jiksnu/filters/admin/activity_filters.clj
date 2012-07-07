(ns jiksnu.filters.admin.activity-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.actions.admin.activity-actions :only [index]]))

(defn parse-page
  [request]
  (or (-?> request :params :page Integer/parseInt) 1))

(deffilter #'index :http
  [action request]
  (let [page (parse-page request)]
    (action {} {:page page})))
