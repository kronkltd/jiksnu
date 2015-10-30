(ns jiksnu.modules.web.views.auth-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]))

(defview #'actions.auth/guest-login :html
  [request user]
  {:status 303
   :template false
   :session {:pending-id (:_id user)}
   :headers {"Location" "/main/password"}})

(defview #'actions.auth/password-page :html
  [request user]
  {:body (sections.auth/password-page user)})
