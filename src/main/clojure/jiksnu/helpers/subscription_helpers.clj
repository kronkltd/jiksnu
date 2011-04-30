(ns jiksnu.helpers.subscription-helpers
  (:use ciste.core
        ciste.debug
        ciste.sections
        ciste.sections.default
        clj-tigase.core
        jiksnu.model
        jiksnu.namespace
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.user :as model.user]))

(defn delete-form
  [subscription]
  (f/form-to [:delete (uri subscription)]
             (f/hidden-field :id (:_id subscription))
             (f/submit-button "Delete")))


(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    ["subscriber" {"node" microblog-uri
                   "created" (format-date (:created subscription))
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))}]))

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))
        created (:created subscription)]
    ["subscription" {"node" microblog-uri
                     "subscription" "subscribed"
                     "created" (format-date created)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))}]))

(defn unsubscription-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" pubsub-uri}
     ["unsubscribe" {"node" microblog-uri
                     "jid" (make-jid subscribee)}]]))

(defn subscribe-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" pubsub-uri}
     ["subscribe" {"node" microblog-uri
                   "jid" (make-jid subscribee)}]]))

(defn subscribers-response
  [subscribers]
  ["pubsub" {"xmlns" pubsub-uri}
   ["subscribers" {"node" microblog-uri}
    (map subscriber-response-element subscribers)]])

(defn subscriptions-response
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  ["pubsub" {"xmlns" pubsub-uri}
   ["subscriptions" {"node" microblog-uri}
    (map subscription-response-element subscriptions)]])
