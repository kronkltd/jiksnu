(ns jiksnu.modules.admin.views.client-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.modules.admin.actions.client-actions
             :as actions.admin.client]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections :refer [format-page-info]])
  (:import jiksnu.model.Client))

(defview #'actions.admin.client/index :html
  [request page]
  {:title "Clients"
   :body (admin-index-section (:items page) page)})

(defview #'actions.admin.client/index :viewmodel
  [request _]
  {:body
   {:title "Clients"}})

