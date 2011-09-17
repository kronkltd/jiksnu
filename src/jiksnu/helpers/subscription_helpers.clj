(ns jiksnu.helpers.subscription-helpers
  (:use (ciste core debug sections)
        ciste.sections.default
        clj-tigase.core
        (jiksnu model namespace view))
  (:require [jiksnu.model.user :as model.user]))

(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    ["subscriber" {"node" namespace/microblog
                   "created" (format-date (:created subscription))
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))}]))

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))
        created (:created subscription)]
    ["subscription" {"node" namespace/microblog
                     "subscription" "subscribed"
                     "created" (format-date created)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))}]))

(defn unsubscription-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" pubsub-uri}
     ["unsubscribe" {"node" namespace/microblog
                     "jid" (make-jid subscribee)}]]))

(defn subscribe-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" namespace/pubsub}
     ["subscribe" {"node" namespace/microblog
                   "jid" (make-jid subscribee)}]]))

(defn subscribers-response
  [subscribers]
  ["pubsub" {"xmlns" namespace/pubsub}
   ["subscribers" {"node" namespace/microblog}
    (map subscriber-response-element subscribers)]])

(defn subscriptions-response
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  ["pubsub" {"xmlns" namespace/pubsub}
   ["subscriptions" {"node" namespace/microblog}
    (map subscription-response-element subscriptions)]])
