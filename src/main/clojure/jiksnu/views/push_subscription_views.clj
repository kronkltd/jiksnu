(ns jiksnu.views.push-subscription-views
  (:use ciste.config
        ciste.debug
        ciste.sections
        ciste.sections.default
        ciste.views
        jiksnu.actions.push-subscription-actions
        jiksnu.helpers.push-subscription-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.push-subscription-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view)
  (:require [jiksnu.model.push-subscription :as model.push-subscription]
            [jiksnu.model.user :as model.user]
            [karras.entity :as entity]
            [hiccup.form-helpers :as f])
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

