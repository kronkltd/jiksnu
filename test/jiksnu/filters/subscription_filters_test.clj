(ns jiksnu.filters.subscription-filters-test
  (:use (ciste [config :only [with-environment]]
               [core :only [with-context]]
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


   (fact "filter-action #'subscribe :html :http"
     (fact "when the user is not already subscribed"
       (fact "should return a subscription"
         (let [user (model.user/create (factory User))
               subscribee (model.user/create (factory User))]
           (model.subscription/drop!)
           (session/with-user user
             (let [request {:params {:subscribeto (str (:_id user))}
                            :serialization :http}]
               (filter-action #'subscribe request) => model/subscription?))))))

   (fact "filter-action #'get-subscribers :xmpp"
     (fact "when there are subscribers"
       (fact "should not be empty"
         (model/drop-all!)
         (with-context [:xmpp :xmpp]
           (let [user (model.user/create (factory User))
                 subscriber (model.user/create (factory User))
                 request (-> (model.subscription/subscribers-request
                              subscriber user)
                             packet/make-request
                             (assoc :serialization :xmpp)
                             (assoc :format :xmpp))]
             (session/with-user subscriber (subscribed subscriber user))
            (let [[returned-user subscribers] (filter-action #'get-subscribers request)]
              returned-user => user
              subscribers =not=> empty?
              subscribers => (partial every? model/subscription?)))))))

   (fact "filter-action [#'get-subscriptions :xmpp]"
     (fact "when there are subscriptions"
       (fact "should return a sequence of subscriptions"
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

   ))
