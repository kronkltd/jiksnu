(ns jiksnu.modules.command.filters.subscription-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model.subscription :as model.subscription]))

(deffilter #'actions.subscription/delete :command
  [action id]
  (when-let [item (model.subscription/fetch-by-id id)]
    (action item)))

