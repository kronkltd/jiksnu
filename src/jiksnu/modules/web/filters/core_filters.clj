(ns jiksnu.module.web.filters.access-token-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.modules.web.actions.core-actions :as actions.web.core]))

(deffilter #'actions.web.core/nav-info :http
  [action request]
  (action))
