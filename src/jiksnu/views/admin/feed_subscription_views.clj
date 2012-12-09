(ns jiksnu.views.admin.feed-subscription-views
  (:use [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-subscription-actions :only [delete index show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section bind-to format-page-info
                                pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSubscription))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Feed Subscriptions"
   :status 200
   :single true
   :body
   (let [items (if *dynamic* [(FeedSubscription.)] items)]
     (with-page "default"
       (pagination-links response)
       (bind-to "items"
         (admin-index-section items response))))})

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
