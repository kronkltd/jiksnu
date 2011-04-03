(ns jiksnu.controller.subscription-controller
  (:use ciste.core
        ciste.debug
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user-id)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(defaction delete
  [& _])

(defaction index
  [& _])

(defaction ostatus
  [& _])

(defaction ostatussub
  [& _])

(defaction ostatussub-submit
  [& _])

(defaction remote-subscribe
  [& _])

(defaction remote-subscribe-confirm
  [& _])

(defaction subscribe
  [& _])

(defaction subscribed
  [& _])

(defaction subscribers
  [& _])

(defaction subscriptions
  [& _])

(defaction unsubscribe
  [& _])

