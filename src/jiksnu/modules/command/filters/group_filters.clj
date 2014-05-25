(ns jiksnu.modules.command.filters.group-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]))

(deffilter #'actions.group/join :command
  [action id]
  (if-let [item (model.group/fetch-by-id id)]
    (action item)))

(deffilter #'actions.group/leave :command
  [action id]
  (if-let [item (model.group/fetch-by-id id)]
    (action item)))
