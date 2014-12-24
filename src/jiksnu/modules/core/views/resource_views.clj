(ns jiksnu.modules.core.views.resource-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section show-section]]
        jiksnu.actions.resource-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links redirect with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Resource))

(defview #'create :html
  [request item]
  (redirect (:url item) "resource has been created"))

(defview #'delete :html
  [request _]
  (redirect "/" "resource has been deleted"))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Resources"
   :single true
   :body (index-section items page)})

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})

(defview #'index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Resources"
          :pages {:resources (format-page-info page)}}})

(defview #'show :html
  [request item]
  {:body
   (let [item (Resource.)]
     (bind-to "targetResource"
       [:div {:data-model "resource"}
        (show-section item)]))})

(defview #'show :model
  [request item]
  {:body item})

(defview #'show :viewmodel
  [request item]
  {:body {:targetResource (:_id item)
          :title (or (:title item) "Resource")}})

