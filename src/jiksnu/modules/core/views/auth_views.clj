(ns jiksnu.modules.core.views.auth-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [jiksnu.actions.auth-actions :as actions.auth]))

(defview #'actions.auth/login :text
  [request user]
  {:session {:id (:_id user)}
   :body (format "logged in as %s" (:username user))})
