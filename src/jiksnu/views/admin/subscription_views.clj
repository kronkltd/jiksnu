(ns jiksnu.views.admin.subscription-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.subscription-actions :only [index delete show]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [clojure.tools.logging :as log]))

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (sections.subscription/index-section subscriptions)})

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Subscriptions"
   :single true
   :body (admin-index-section items response)})

(defview #'show :html
  [request subscription]
  {:title "Subscription"
   :body (show-section subscription)})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "subscription deleted"
   :headers {"Location" "/admin/subscriptions"}})
