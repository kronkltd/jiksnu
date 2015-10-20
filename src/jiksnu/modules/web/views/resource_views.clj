(ns jiksnu.modules.web.views.resource-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [taoensso.timbre :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.modules.web.sections :refer [redirect]]))

(defview #'actions.resource/create :html
  [request item]
  (redirect (:url item) "resource has been created"))

(defview #'actions.resource/delete :html
  [request _]
  (redirect "/" "resource has been deleted"))

(defview #'actions.resource/index :html
  [request {:keys [items] :as page}]
  {:title "Resources"
   :single true
   :body (index-section items page)})

(defview #'actions.resource/show :html
  [request item]
  {:body
   [:div {:data-model "resource"}
    (show-section item)]})
