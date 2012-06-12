(ns jiksnu.views.admin.feed-subscription-views
  (:use [ciste.debug :only [spy]]
        [ciste.sections.default :only [add-form index-section title show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.feed-subscription-actions :only [delete index show]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [ring.util.response :as response])
  (:import jiksnu.model.FeedSubscription))

(defview #'index :html
  [request [subscriptions options]]
  {:title "Feed Subscriptions"
   :single true
   :body
   (list 
    (index-section subscriptions)
    (add-form (model/->FeedSubscription)))})

;; (defview #'show :html
;;   [request subscription]
;;   {:title (title subscription)
;;    :single true
;;    :body (list (show-section subscription)
;;                (index-watchers subscription)
;;                (add-watcher-form subscription)
;;                )})

;; (defview #'delete :html
;;   [request subscription]
;;   (-> (response/redirect-after-post "/admin/feed-subscriptions")
;;         (assoc :template false)))
