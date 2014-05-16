(ns jiksnu.modules.core.filters.group-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]))

(deffilter #'actions.group/index :page
  [action request]
  (action))

(deffilter #'actions.group/fetch-admins :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.group/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))


