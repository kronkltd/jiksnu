(ns jiksnu.modules.admin.views.request-token-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.admin.actions.request-token-actions
             :as actions.admin.request-token]
            [jiksnu.modules.core.sections :refer [admin-index-section]])
  (:import jiksnu.model.RequestToken))

(defview #'actions.admin.request-token/index :html
  [request response]
  {:title "Request Tokens"
   :body (admin-index-section (:items page) page)})

(defview #'actions.admin.request-token/index :viewmodel
  [request response]
  {:body
   {:title "Request Tokens"}})
