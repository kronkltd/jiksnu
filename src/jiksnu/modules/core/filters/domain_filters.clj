(ns jiksnu.modules.core.filters.domain-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.domain-actions :as actions.domain]))

(deffilter #'actions.domain/index :page
  [action request]
  (action))
