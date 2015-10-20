(ns jiksnu.modules.web.views.client-views
  (:require [ciste.views :refer [defview]]
            [taoensso.timbre :as log]
            [jiksnu.actions.client-actions :as actions.client]))

(defview #'actions.client/index :page
  [request page]
  (let [items (:items page)
        page (merge page
                    {:id (:name request)
                     :items (map :_id items)})]
    {:body {:action "page-updated"
            :body page}}))
