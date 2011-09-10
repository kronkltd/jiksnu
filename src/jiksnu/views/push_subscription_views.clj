(ns jiksnu.views.push-subscription-views
  (:use (ciste [config :only (config)]
               [debug :only (spy)]
               sections
               [views :only (defview)])
        ciste.sections.default
        jiksnu.actions.push-subscription-actions
        (jiksnu model session view))
  (:require (jiksnu [namespace :as namespace])
            (jiksnu.model [push-subscription :as model.push-subscription]
                          [user :as model.user]))
  (:import jiksnu.model.PushSubscription
           jiksnu.model.User))


(defview #'callback :html
  [request params]
  {:body params
   :template false})

(defview #'callback-publish :html
  [request params]
  {:body params
   :template false})

(defview #'index :html
  [request subscriptions]
  {:body (index-section subscriptions)})

(defview #'hub :html
  [request _]
  {:template :false})

(defview #'hub-publish :html
  [request response]
  (merge {:template :false}
         response))

(defview #'subscribe :html
  [request _]
  {:template :false})

