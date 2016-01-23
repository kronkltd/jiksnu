(ns jiksnu.modules.core.views.feed-subscription-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]))

(defview #'actions.feed-subscription/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))
