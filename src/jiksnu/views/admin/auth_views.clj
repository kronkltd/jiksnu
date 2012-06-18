(ns jiksnu.views.admin.auth-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [add-form]]
        [jiksnu.actions.admin.auth-actions :only [index]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [jiksnu.model :as model]))

;; TODO: This page should use a single column
(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Authentication Mechanisms"
   :single true
   :body (list (admin-index-section items response)
               (add-form (model/->AuthenticationMechanism)))})
