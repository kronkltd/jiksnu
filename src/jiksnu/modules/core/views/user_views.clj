(ns jiksnu.modules.core.views.user-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.web.sections :refer [format-page-info]]))

;; index

(defview #'actions.user/index :json
  [request {:keys [items] :as options}]
  {:body
   {:items (index-section items options)}})

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

;; register-page

(defview #'actions.user/register-page :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Register"}})

(defview #'actions.user/profile :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Profile"}})

;; show

(defview #'actions.user/show :as
  [request user]
  {:template false
   :body (show-section user)})

(defview #'actions.user/show :model
  [request user]
  {:body (doall (show-section user))})

