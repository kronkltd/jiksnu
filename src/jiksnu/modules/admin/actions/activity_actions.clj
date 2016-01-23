(ns jiksnu.modules.admin.actions.activity-actions
  "This is the namespace for the admin pages for activities"
  (:require [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.activity))

(defn index
  [& [params & [options]]]
  (index* params options))
