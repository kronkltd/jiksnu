(ns jiksnu.views.admin.feed-subscription-views
  (:use [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-subscription-actions :only [delete index show]]
        [jiksnu.sections :only [admin-index-section format-page-info pagination-links with-page]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [ring.util.response :as response]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Feed Subscriptions"
   :status 200
   :single true
   :body
   (with-page "default"
     (pagination-links response)
     (admin-index-section items response))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Subscriptions"
          :pages {:default (format-page-info page)}}})



;; (defview #'show :html
;;   [request subscription]
;;   {:title (title subscription)
;;    :single true
;;    :body (list (show-section subscription)
;;                (index-watchers subscription)
;;                (add-watcher-form subscription))})

;; (defview #'delete :html
;;   [request subscription]
;;   (-> (response/redirect-after-post "/admin/feed-subscriptions")
;;         (assoc :template false)))
