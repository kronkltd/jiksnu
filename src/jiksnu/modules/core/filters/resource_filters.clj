(ns jiksnu.modules.core.filters.resource-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.resource-actions :as actions.resource]))

(deffilter #'actions.resource/index :page
  [action request]
  (action))
