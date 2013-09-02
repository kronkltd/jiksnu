(ns jiksnu.modules.core.filters.subscription-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.subscription-actions :as actions.subscription]))

(deffilter #'actions.subscription/get-subscribers :page
  [action request]
  (let [item (:item request)]
    (action item)))

(deffilter #'actions.subscription/get-subscriptions :page
  [action request]
  (let [item (:item request)]
    (action item)))

(deffilter #'actions.subscription/index :page
  [action request]
  (action))

