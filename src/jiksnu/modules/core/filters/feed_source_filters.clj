(ns jiksnu.modules.core.filters.feed-source-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]))

(deffilter #'actions.feed-source/index :page
  [action request]
  (action))
