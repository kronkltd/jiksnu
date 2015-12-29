(ns jiksnu.modules.core.filters.conversation-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.conversation-actions :as actions.conversation]))

(deffilter #'actions.conversation/index :page
  [action request]
  (action))

(deffilter #'actions.conversation/fetch-by-group :page
  [action request]
  (action))
