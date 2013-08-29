(ns jiksnu.modules.admin.views.client-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.admin.actions.client-actions
             :as actions.admin.client]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections :refer [format-page-info pagination-links
                                                 with-page]])
  (:import jiksnu.model.Client)
  )

(defview #'actions.admin.client/index :html
  [request response]
  {:title "Clients"
   :body
   (let [page (if *dynamic*
                {:items [(Client.)]}
                response)]
     (with-page "clients"
       (pagination-links page)
       (admin-index-section (:items page) page)))})

(defview #'actions.admin.client/index :viewmodel
  [request response]
  {:body
   {:title "Clients"}})

