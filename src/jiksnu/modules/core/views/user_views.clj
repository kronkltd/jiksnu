(ns jiksnu.modules.core.views.user-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.core.sections :refer [format-page-info]]))

(defview #'actions.user/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.user/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Users"
          :pages {:users (format-page-info page)}}})

(defview #'actions.user/register-page :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Register"}})

(defview #'actions.user/profile :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Profile"}})

(defview #'actions.user/show :model
  [request user]
  {:body (doall (show-section user))})

