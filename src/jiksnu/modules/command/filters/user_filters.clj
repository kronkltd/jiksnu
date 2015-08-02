(ns jiksnu.modules.command.filters.user-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.user/delete :command
  [action id]
  (when-let [item (model.user/fetch-by-id id)]
    (action item)))

(deffilter #'actions.user/discover :command
  [action id]
  (if-let [item (model.user/fetch-by-id id)]
    (action item {:force true})))

(deffilter #'actions.user/subscribe :command
  [action id]
  (when-let [item (model.user/fetch-by-id id)]
    (action item)))

(deffilter #'actions.user/update-record :command
  [action id]
  (let [item (model.user/fetch-by-id id)]
    (action item {:force true})))

