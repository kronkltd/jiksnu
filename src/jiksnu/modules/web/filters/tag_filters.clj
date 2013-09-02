(ns jiksnu.modules.web.filters.tag-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.tag-actions :as actions.tag]))

(deffilter #'actions.tag/show :http
  [action request]
  (-> request :params :name action))
