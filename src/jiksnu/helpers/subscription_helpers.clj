(ns jiksnu.helpers.subscription-helpers
  (:use (ciste [debug :only [spy]])
        ciste.sections.default
        clj-tigase.core
        (jiksnu [model :only [format-date]]))
  (:require (jiksnu [namespace :as ns])
            (jiksnu.model [user :as model.user])))

(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    ["subscriber" {"node" ns/microblog
                   "created" (format-date (:created subscription))
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))}]))

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))
        created (:created subscription)]
    ["subscription" {"node" ns/microblog
                     "subscription" "subscribed"
                     "created" (format-date created)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))}]))

(defn unsubscription-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" ns/pubsub}
     ["unsubscribe" {"node" ns/microblog
                     "jid" (make-jid subscribee)}]]))

(defn subscribe-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" ns/pubsub}
     ["subscribe" {"node" ns/microblog
                   "jid" (make-jid subscribee)}]]))

(defn subscribers-response
  [subscribers]
  ["pubsub" {"xmlns" ns/pubsub}
   ["subscribers" {"node" ns/microblog}
    (map subscriber-response-element subscribers)]])

(defn subscriptions-response
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  ["pubsub" {"xmlns" ns/pubsub}
   ["subscriptions" {"node" ns/microblog}
    (map subscription-response-element subscriptions)]])
