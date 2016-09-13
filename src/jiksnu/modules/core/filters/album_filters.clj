(ns jiksnu.modules.core.filters.album-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.album-actions :as actions.album]))

(deffilter #'actions.album/index :page
  [action request]
  (action))

(deffilter #'actions.album/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))
