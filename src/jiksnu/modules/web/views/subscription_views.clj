(ns jiksnu.modules.core.views.subscription-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section uri]]
        jiksnu.actions.subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info with-page with-sub-page
                                            pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.modules.core.sections.subscription-sections :as sections.subscription]
            [jiksnu.modules.web.sections.subscription-sections :refer [ostatus-sub-form]])
  (:import jiksnu.model.Subscription))

(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})

;; get-subscribers

(defview #'get-subscribers :html
  [request [user {:keys [items] :as page}]]
  {:title "Subscribers"
   :body
   (let [items (if *dynamic* [(Subscription.)] items)]
     (bind-to "targetUser"
       [:div {:data-model "user"}
        (with-sub-page "subscribers"
          (pagination-links page)
          (sections.subscription/subscribers-section items page))]))})

(defview #'get-subscriptions :html
  [request [user {:keys [items] :as page}]]
  {:title "Subscriptions"
   :formats (subscription-formats user)
   :body
   (if-let [items (seq (if *dynamic* [(Subscription.)] items))]
     (bind-to "targetUser"
       [:div {:data-model "user"}
        (with-sub-page "subscriptions"
          (pagination-links page)
          (sections.subscription/subscriptions-section items page))]))})

;; ostatus

(defview #'ostatus :html
  [request arg]
  {:body ""
   :template false})

;; ostatussub

(defview #'ostatussub :html
  [request arg]
  {:body (ostatus-sub-form)})

;; ostatussub-submit

(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})

;; subscribe

(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

;; unsubscribe

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

