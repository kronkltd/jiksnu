(ns jiksnu.modules.core.views.domain-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.sections :refer [format-page-info]]))

(defview #'actions.domain/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))
