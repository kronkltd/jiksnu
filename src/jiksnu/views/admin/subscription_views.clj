(ns jiksnu.views.admin.subscription-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.subscription-actions :only [index delete show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section admin-show-section]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.Subscription))

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (sections.subscription/index-section subscriptions)})

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Subscriptions"
   :single true
   :viewmodel "/admin/subscriptions.viewmodel"
   :body (admin-index-section (if *dynamic*
                                (Subscription.)
                                items) response)})

(defview #'index :viewmodel
  [request {:keys [items] :as response}]
  {:body {:title "Subscriptions"
          :items (map :_id items)
          :subscriptions (admin-index-section items response)}})

(defview #'show :html
  [request subscription]
  {:title "Subscription"
   :body (admin-show-section subscription)})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "subscription deleted"
   :template false
   :headers {"Location" "/admin/subscriptions"}})
