(ns jiksnu.modules.admin.actions.activity-actions
  "This is the namespace for the admin pages for activities"
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.activity))

(defaction index
  [& [params & [options]]]
  (index* params options))
