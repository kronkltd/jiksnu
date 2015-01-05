(ns jiksnu.modules.command.filters.resource-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.model.resource :as model.resource]))

(deffilter #'actions.resource/delete :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

;; discover

(deffilter #'actions.resource/discover :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

;; update

(deffilter #'actions.resource/update* :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))


(deffilter #'actions.resource/update :command
  [action id]
  (when-let [item (model.resource/fetch-by-id id)]
    (action item)))

