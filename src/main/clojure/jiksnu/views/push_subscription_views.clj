(ns jiksnu.views.push-subscription-views
  (:use [ciste.config :only (config)]
        ciste.core
        ciste.debug
        ciste.html
        ciste.sections
        ciste.view
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


(defview #'index :html
  [request subscriptions]
  {:body (index-section subscriptions)})
