(ns jiksnu.modules.admin.views.subscription-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.subscription-actions :only [index delete show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [admin-index-section admin-show-section bind-to
                                dump-data format-page-info pagination-links
                                with-page]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.Subscription))

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (sections.subscription/index-section subscriptions)})

;; delete

(defview #'delete :html
  [request _]
  {:status 303
   :flash "subscription deleted"
   :template false
   :headers {"Location" "/admin/subscriptions"}})

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  (let [subscriptions (if *dynamic* [(Subscription.)] items)]
    {:title "Subscriptions"
     :status 200
     :single true
     :body (with-page "subscriptions"
             (pagination-links page)
             (admin-index-section subscriptions page))}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Subscriptions"
          :items (map :_id items)
          :pages {:subscriptions (format-page-info page)}
          :subscriptions (doall (admin-index-section items page))}})

;; show

(defview #'show :html
  [request subscription]
  {:title "Subscription"
   :body (admin-show-section subscription)})

(defview #'show :model
  [request subscription]
  {:body (show-section subscription)})

(defview #'show :viewmodel
  [request subscription]
  {:body {:title "Subscription"
          :subscriptions (admin-index-section [subscription])
          :targetSubscription (:_id subscription)}})
