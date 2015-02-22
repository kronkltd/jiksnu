(ns jiksnu.modules.command.filters.domain-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.model.domain :as model.domain]))

(deffilter #'actions.domain/delete :command
  [action id]
  (when-let [item (model.domain/fetch-by-id id)]
    (action item)))

(deffilter #'actions.service/discover :command
  [action id]
  (when-let [item (model.domain/fetch-by-id id)]
    (first (action item))))

