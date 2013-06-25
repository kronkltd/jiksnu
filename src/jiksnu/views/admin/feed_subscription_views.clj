(ns jiksnu.views.admin.feed-subscription-views
  (:use [ciste.sections.default :only [title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-subscription-actions :only [delete index
                                                               show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section format-page-info
                                pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSubscription))

(defview #'index :html
  [request {:keys [items] :as page}]
  (let [items (if *dynamic* [(FeedSubscription.)] items)]
    {:title "Feed Subscriptions"
     :status 200
     :single true
     :body (with-page "feedSubscriptions"
             (pagination-links page)
             (admin-index-section items page))}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Feed Subscriptions"
          :pages {:feedSubscriptions (format-page-info page)}}})

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
