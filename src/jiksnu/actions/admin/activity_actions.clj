(ns jiksnu.actions.admin.activity-actions
  "This is the namespace for the admin pages for activities"
  (:use [ciste.core :only [defaction]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.activity))

(defaction index
  [& [params & [options]]]
  (index* params options))
