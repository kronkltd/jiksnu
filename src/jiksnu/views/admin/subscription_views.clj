(ns jiksnu.views.admin.subscription-views
  (:use (ciste [debug :only [spy]]
               [views :only [defview]])
        jiksnu.actions.admin.subscription-actions
        jiksnu.sections.subscription-sections)
  (:require (jiksnu.model [subscription :as model.subscription])))

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (sections.subscription/index-section subscriptions)})

(defview #'index :html
  [request [subscriptions options]]
  {:title "Subscriptions"
   :single true
   :body (admin-index-section subscriptions)})
