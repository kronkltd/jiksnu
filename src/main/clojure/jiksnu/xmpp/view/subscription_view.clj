(ns jiksnu.xmpp.view.subscription-view
  (:use ciste.core
        ciste.view
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.controller.subscription-controller
        jiksnu.xmpp.view)
  (:require [jiksnu.model.subscription :as model.subscription])
  (:import tigase.xml.Element))

(defn subscriber-response-element
  [subscriber]
  (make-element
   "subscriber" {"node" microblog-uri
                 "created" (str (:created subscriber))
                 "jid" (str (:from subscriber))}))

(defn minimal-subscriber-response
  [subscribers]
  (make-element
   "pubsub" {"xmlns" pubsub-uri}
   [(make-element
     "subscribers" {"node" microblog-uri}
     (map subscriber-response-element subscribers))]))

(defn subscription-response-element
  [subscription]
  (make-element
   "subscription" {"node" microblog-uri
                   "subscription" "subscribed"
                   "created" (str (:created subscription))
                   "jid" (:to subscription)}))

(defn minimal-subscription-response
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  (make-element
   "pubsub" {"xmlns" pubsub-uri}
   [(make-element
     "subscriptions" {"node" microblog-uri}
     (map subscription-response-element subscriptions))]))

(defview #'subscriptions :xmpp
  [request subscriptions]
  {:body (minimal-subscription-response subscriptions)
   :from (:to request)
   :to (:from request)})

(defview #'subscribers :xmpp
  [request subscribers]
  (println "subscribers: " subscribers)
  {:body (minimal-subscriber-response nil)
   :from (:to request)
   :to (:from request)})

(defview #'subscribe :xmpp
  [request subscription]
  {:body subscription
   :from (:to request)
   :to (:from request)})

(defview #'subscribed :xmpp
  [request subscription]
  {:body subscription
   :from (:to request)
   :to (:from request)})
