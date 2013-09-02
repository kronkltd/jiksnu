(ns jiksnu.modules.core.filters.feed-subscription-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]))

(deffilter #'actions.feed-subscription/index :page
  [action request]
  (action))

