(ns jiksnu.modules.core.views.resource-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [jiksnu.actions.resource-actions :as actions.resource]))

(defview #'actions.resource/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))
