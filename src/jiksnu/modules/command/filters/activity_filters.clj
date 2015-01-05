(ns jiksnu.modules.command.filters.activity-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]))

(deffilter #'actions.activity/delete :command
  [action id]
  (let [item (model.activity/fetch-by-id id)]
    (action item)))

