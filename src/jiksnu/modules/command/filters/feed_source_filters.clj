(ns jiksnu.modules.command.filters.feed-source-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

;; delete

(deffilter #'actions.feed-source/delete :command
  [action id]
  (if-let [item (model.feed-source/fetch-by-id id)]
    (action item)))

(deffilter #'actions.feed-source/unsubscribe :command
  [action id]
  (if-let [item (model.feed-source/fetch-by-id id)]
    (action item)))

;; subscribe

(deffilter #'actions.feed-source/subscribe :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item)))

;; update

(deffilter #'actions.feed-source/update :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item {:force true})))

;; watch

(deffilter #'actions.feed-source/watch :command
  [action id]
  (action (model.feed-source/fetch-by-id id)))
