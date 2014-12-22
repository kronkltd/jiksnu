(ns jiksnu.modules.web.filters.site-filters
  (:use [ciste.filters :refer [deffilter]]
        [jiksnu.actions.site-actions :as actions.site]
        [jiksnu.session :as session]))

(deffilter #'actions.site/get-stats :http
  [action request]
  (action))

(deffilter #'actions.site/rsd :http
  [action request]
  (action))

(deffilter #'actions.site/service :http
  [action request]
  (action (current-user-id)))

(deffilter #'actions.site/status :http
  [action request]
  (action))
