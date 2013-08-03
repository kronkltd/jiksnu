(ns jiksnu.modules.core.views.resource-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section show-section]]
        jiksnu.actions.resource-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [ring.util.response :as response])
  (:import jiksnu.model.Resource))

;; create

(defview #'create :html
  [request item]
  (-> (response/redirect-after-post (:url item))
      (assoc :template false)
      (assoc :flash "resource has been created")))

;; delete

(defview #'delete :html
  [request _]
  (-> (response/redirect-after-post "/")
      (assoc :template false)
      (assoc :flash "resource has been deleted")))

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Resources"
   :single true
   :body
   (let [items (if *dynamic* [(Resource.)] items)]
     (with-page "resources"
       (pagination-links page)
       (doall (index-section items page))))})

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

;; show

(defview #'show :html
  [request item]
  {:body
   (let [item (if *dynamic* (Resource.) item)]
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

