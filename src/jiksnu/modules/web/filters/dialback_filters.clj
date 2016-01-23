(ns jiksnu.modules.web.filters.dialback-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.dialback-actions :as actions.dialback]))

(deffilter #'actions.dialback/confirm :http
  [action request]
  (action (:params request)))
