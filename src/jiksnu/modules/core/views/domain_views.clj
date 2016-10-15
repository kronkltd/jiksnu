(ns jiksnu.modules.core.views.domain-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.domain-actions :as actions.domain]))

(defview #'actions.domain/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))
