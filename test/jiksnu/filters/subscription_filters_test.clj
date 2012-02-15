(ns jiksnu.filters.subscription-filters-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]]
               filters)
        (clj-factory [core :only [factory fseq]])
        (jiksnu test-helper)
        jiksnu.actions.subscription-actions
        jiksnu.filters.subscription-filters
        midje.sweet)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [model :as model]
                    [namespace :as namespace]
                    [session :as session])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(test-environment-fixture

 (against-background
   [(around :facts
            (do (model.user/drop!)
                (model.subscription/drop!)
                ?form))]


   (future-fact "filter-action #'subscribe :html :http"
     (fact "when the user is not already subscribed"
       (fact "should return a subscription"
         (let [user (model.user/create (factory User))
               subscribee (model.user/create (factory User))]
           (model.subscription/drop!)
           (session/with-user user
             (let [request {:params {:subscribeto (str (:_id user))}
                            :serialization :http}]
               (filter-action #'subscribe request) => model/subscription?))))))

   (future-fact "filter-action #'get-subscribers :xmpp"
     (fact "when there are subscribers"
       (fact "should not be empty"
         (let [user (actions.user/create (factory User))
               subscriber (actions.user/create (factory User))
               request (-> (model.subscription/subscribers-request
                            user subscriber)
                           packet/make-request
                           (assoc :serialization :xmpp))]
           (session/with-user subscriber (subscribe user))
           (let [[user subscribers] (filter-action #'get-subscribers request)]
             subscribers =not=> empty?
             subscribers => (partial every? model/subscription?))))))

   ;; (deftest filter-action-test "#'get-subscriptions :xmpp"
   ;;   (fact "when there are subscriptions"))

   (future-fact "should return a sequence of subscriptions"
     (let [user (actions.user/create (factory User))
           subscribee (actions.user/create (factory User))
           request (-> (model.subscription/subscriptions-request
                        subscribee user)
                       packet/make-request
                       (assoc :serialization :xmpp))]
       (session/with-user user (subscribe user subscribee))
       (let [[user2 subscriptions :as response]
             (filter-action #'get-subscriptions request)]
         user2 => user
         subscriptions =not=> empty?
         subscriptions => (partial every? model/subscription?))))))
