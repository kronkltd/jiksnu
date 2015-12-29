(ns jiksnu.modules.core.filters.activity-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.activity-actions :as actions.activity]))

(deffilter #'actions.activity/fetch-by-conversation :page
  [action request]
  (when-let [conversation (:item request)]
    (action conversation)))

(deffilter #'actions.activity/index :page
  [action request]
  (action))
