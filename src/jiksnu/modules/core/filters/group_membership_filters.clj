(ns jiksnu.modules.core.filters.group-membership-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]))

(deffilter #'actions.group-membership/index :page
  [action request]
  (action))

(deffilter #'actions.group-membership/fetch-by-group :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.group-membership/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))
