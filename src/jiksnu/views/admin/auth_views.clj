(ns jiksnu.views.admin.auth-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section add-form]]
        [jiksnu.actions.admin.auth-actions :only [index]])
  (:require [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model :as model])
  (:import jiksnu.model.AuthenticationMechanism))

;; TODO: This page should use a single column
(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Authentication Mechanisms"
   :single true
   :body
   (list (index-section items response)
         (add-form (model/->AuthenticationMechanism)))})
