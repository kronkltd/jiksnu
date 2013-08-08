(ns jiksnu.modules.admin.views.auth-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [add-form]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.admin.actions.auth-actions :only [index]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info
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
