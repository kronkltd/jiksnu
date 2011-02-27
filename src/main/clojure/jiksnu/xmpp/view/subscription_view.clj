(ns jiksnu.xmpp.view.subscription-view
  (:use ciste.core
        ciste.view
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.controller.subscription-controller
        jiksnu.xmpp.view)
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import tigase.xml.Element
           java.text.SimpleDateFormat))

(defonce xsd-formatter
  (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss'Z'"))

(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    (make-element
     "subscriber" {"node" microblog-uri
                   "created" (.format xsd-formatter (:created subscription))
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))})))

(defn minimal-subscriber-response
  [subscribers]
  (make-element
   "pubsub" {"xmlns" pubsub-uri}
   ["subscribers" {"node" microblog-uri}
    (map subscriber-response-element subscribers)]))

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))
        created (:created subscription)]
    (make-element
     "subscription" {"node" microblog-uri
                     "subscription" "subscribed"
                     "created" (.format xsd-formatter created)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))})))

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
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    (minimal-subscription-response subscriptions))
   :from (:to request)
   :to (:from request)})

(defview #'subscribers :xmpp
  [request subscribers]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    (minimal-subscriber-response subscribers))
   :from (:to request)
   :to (:from request)})

(defview #'subscribe :xmpp
  [request subscription]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    (subscription-response-element subscription))
   :from (:to request)
   :to (:from request)})

(defview #'unsubscribe :xmpp
  [request subscription]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)})
   :from (:to request)
   :to (:from request)})

(defview #'subscribed :xmpp
  [request subscription]
  {:body subscription
   :from (:to request)
   :to (:from request)})

(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)
