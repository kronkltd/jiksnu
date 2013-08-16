(ns jiksnu.modules.admin.views.stream-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.admin.actions.stream-actions :as actions.admin.stream]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections :refer [pagination-links with-page]])
  (:import jiksnu.model.Stream))

(defview #'actions.admin.stream/index :html
  [request page]
  (let [page (if *dynamic* {:items [(Stream.)]} page)]
    {:title "Streams"
     :single true
     :body (with-page "streams"
             (pagination-links page)
             (admin-index-section (:items page) page))}))

(defview #'actions.admin.stream/index :page
  [request page]
  {:body {:action "page-updated"
          :body (merge page
                       {:id (:name request)
                        :items (map :_id (:items page))})}})

(defview #'actions.admin.stream/index :viewmodel
  [request page]
  {:body {:title "Streams"
          :single true}})
