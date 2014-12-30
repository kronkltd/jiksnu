(ns jiksnu.modules.json.views.domain-views
  (:require [ciste.sections.default :only [index-section
                                           show-section]]
            [ciste.views :only [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.namespace :as ns]))

(defview #'actions.domain/index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})

(defview #'actions.domain/show :json
  [request domain]
  {:body (show-section domain)})

