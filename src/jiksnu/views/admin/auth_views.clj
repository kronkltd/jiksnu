(ns jiksnu.views.admin.auth-views
  (:use (ciste [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.admin.auth-actions)
  (:require (jiksnu.actions [auth-actions :as actions.auth])
            [jiksnu.model :as model])
  (:import jiksnu.model.AuthenticationMechanism))

;; TODO: This page should use a single column
(defview #'index :html
  [request mechanisms]
  {:title "Authentication Mechanisms"
   :single true
   :body
   (list (index-section mechanisms)
         (add-form (model/map->AuthenticationMechanism)))})
