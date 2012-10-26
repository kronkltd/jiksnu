(ns jiksnu.views.resource-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        jiksnu.actions.resource-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [ring.util.response :as response]))

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
   :body
   (with-page "default"
     (list
      (pagination-links page)
      [:div (if *dynamic*
              {:data-bind "with: items"})
       (index-section items page)]))})

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Resources"
          :pages {:default (format-page-info page)}}})

;; show

(defview #'show :model
  [request item]
  {:body (doall (show-section item))})
