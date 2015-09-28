(ns jiksnu.modules.admin.views.stream-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.modules.admin.actions.stream-actions :as actions.admin.stream]
            [jiksnu.modules.core.sections :refer [admin-index-section]])
  (:import jiksnu.model.Stream))

(defview #'actions.admin.stream/index :html
  [request page]
  (let [page {:items [(Stream.)]}]
    {:title "Streams"
     :single true
     :body (admin-index-section (:items page) page)}))

(defview #'actions.admin.stream/index :page
  [request page]
  {:body {:action "page-updated"
          :body (merge page
                       {:id (:name request)
                        :items (map :_id (:items page))})}})
