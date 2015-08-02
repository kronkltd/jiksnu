(ns jiksnu.modules.command.filters.conversation-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model.conversation :as model.conversation]))

(deffilter #'actions.conversation/delete :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item)))

(deffilter #'actions.conversation/discover :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item)))

(deffilter #'actions.conversation/update-record :command
  [action id]
  (when-let [item (model.conversation/fetch-by-id id)]
    (action item {:force true})))
