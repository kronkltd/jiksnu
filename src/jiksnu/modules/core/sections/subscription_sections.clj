(ns jiksnu.modules.core.sections.subscription-sections
  (:use [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [edit-button index-block index-line
                                       index-section link-to show-section
                                       uri]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line
                                             admin-index-section]]
        [jiksnu.modules.web.sections :only [bind-to control-line dump-data with-page
                                            with-sub-page]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

;; subscriptions where the user is the target
(declare-section subscribers-section :seq)
(declare-section subscribers-block :seq)
(declare-section subscribers-line)

;; subscriptions where the user is the actor
(declare-section subscriptions-section :seq)
(declare-section subscriptions-block :seq)
(declare-section subscriptions-line)

(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    ["subscriber" {"node" ns/microblog
                   "created" (:created subscription)
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))}]))

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))]
    ["subscription" {"node" ns/microblog
                     "subscription" "subscribed"
                     "created" (:created subscription)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))}]))

(defn unsubscription-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" ns/pubsub}
     ["unsubscribe" {"node" ns/microblog
                     "jid" (tigase/make-jid subscribee)}]]))

(defn subscribe-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" ns/pubsub}
     ["subscribe" {"node" ns/microblog
                   "jid" (tigase/make-jid subscribee)}]]))

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

(defsection admin-index-line [Subscription :viewmodel]
  [item & [page]]
  item)

(defsection admin-index-block [Subscription :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; index-block

(defsection index-block [Subscription :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (index-line m page)}))
       (into {})))

;; index-line

(defsection index-line [Subscription :as]
  [subscription & _]
  (let [actor (model.subscription/get-actor subscription)
        target (model.subscription/get-target subscription)]
    {:verb "follow"
     :actor (show-section actor)
     :target (show-section target)}))

(defsection index-line [Subscription :viewmodel]
  [item & [page]]
  item)

;; index-section

(defsection index-section [Subscription :viewmodel]
  [items & [page]]
  (index-block items page))

(defsection show-section [Subscription :model]
  [item & _]
  item)

(defsection uri [Subscription]
  [subscription & _]
  (str "/admin/subscriptions/" (:_id subscription)))

