(ns jiksnu.modules.admin.filters.client-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.modules.admin.actions.client-actions
             :as actions.admin.client]))

(deffilter #'actions.admin.client/index :http
  [action request]
  (action))
