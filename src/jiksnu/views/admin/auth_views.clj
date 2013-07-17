(ns jiksnu.views.admin.auth-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [add-form index-block index-section]]
        [jiksnu.actions.admin.auth-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section bind-to format-page-info
                                pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model])
  (:import jiksnu.model.AuthenticationMechanism))

;; TODO: This page should use a single column
(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Authentication Mechanisms"
   :single true
   :body (with-page "mechanisms"
           (pagination-links response)
           (admin-index-section (if *dynamic*
                                  [(AuthenticationMechanism.)]
                                  items)
                                response)
           (add-form (model/->AuthenticationMechanism)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body
   {:title "Authentication Mechanisms"
    :pages {:authMechanisms (format-page-info page)}
    :authenticationMechanisms (admin-index-section items page)}})
