(ns jiksnu.modules.admin.views.auth-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [add-form]]
            [taoensso.timbre :as log]
            [jiksnu.model :as model]
            [jiksnu.modules.admin.actions.auth-actions :refer [index]]
            [jiksnu.modules.core.sections :refer [admin-index-section format-page-info]]))

;; TODO: This page should use a single column
(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Authentication Mechanisms"
   :single true
   :body
   [:div
    (admin-index-section items response)
    (add-form (model/->AuthenticationMechanism))]})
