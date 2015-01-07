(ns jiksnu.modules.core.views.auth-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]))

(defview #'actions.auth/login :text
  [request user]
  {:session {:id (:_id user)}
   :body (format "logged in as %s" (:username user))})

(defview #'actions.auth/show :model
  [request item]
  {:body (doall (show-section item))})

(defview #'actions.auth/whoami :text
  [request user]
  {:body user})

