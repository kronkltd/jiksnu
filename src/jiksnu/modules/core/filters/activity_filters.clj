(ns jiksnu.modules.core.filters.activity-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.activity-actions
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.util :as util]
            [lamina.trace :as trace]))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.activity/fetch-by-id id)]
    (action item)))

;; fetch-by-conversation

(deffilter #'fetch-by-conversation :page
  [action request]
  (when-let [conversation (:item request)]
    (action conversation)))

;; index

(deffilter #'index :page
  [action request]
  (action))

