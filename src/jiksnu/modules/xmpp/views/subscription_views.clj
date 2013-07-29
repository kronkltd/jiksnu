(ns jiksnu.views.subscription-views
  (:use [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info with-page with-sub-page pagination-links]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.sections.subscription-sections :as sections.subscription])
  (:import jiksnu.model.Subscription))

(defview #'get-subscribers :xmpp
  [request [user {:keys [items] :as response}]]
  (tigase/result-packet
   request (sections.subscription/subscribers-response items response)))

(defview #'get-subscriptions :xmpp
  [request [user {:keys [items] :as response}]]
  (tigase/result-packet
   request (sections.subscription/subscriptions-response items response)))

;; remote-subscribe-confirm

(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)

(defview #'subscribe :xmpp
  [request subscription]
  (tigase/result-packet
   request (sections.subscription/subscription-response-element subscription)))

;; subscribed

(defview #'subscribed :xmpp
  [request subscription]
  (tigase/result-packet
   request (sections.subscription/subscriptions-response [subscription])))

(defview #'unsubscribe :xmpp
  [request subscription]
  {:to (-> subscription model.subscription/get-target tigase/make-jid)
   :from (-> subscription model.subscription/get-actor tigase/make-jid)
   :type :result
   :body (sections.subscription/subscriptions-response [subscription])
   :id (:id request)})
