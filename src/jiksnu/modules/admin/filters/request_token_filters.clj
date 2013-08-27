(ns jiksnu.modules.admin.filters.request-token-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.modules.admin.actions.request-token-actions
             :as actions.admin.request-token]))

(deffilter #'actions.admin.request-token/index :http
  [action request]
  (action))
