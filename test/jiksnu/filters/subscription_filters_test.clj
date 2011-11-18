(ns jiksnu.filters.subscription-filters-test
  (:use (ciste [debug :only [spy]]
               filters)
        (clj-factory [core :only [factory fseq]])
        clojure.test
        jiksnu.core-test
        jiksnu.actions.subscription-actions
        jiksnu.filters.subscription-filters
        midje.sweet)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [model :as model]
                    [namespace :as namespace]
                    [session :as session]
                    [view :as view])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(against-background
  [(around :facts
     (do (model.user/drop!)
         (model.subscription/drop!)
         ?form))]


  (deftest filter-action-test "#'subscribe :html :http"
    (testing "when the user is not already subscribed"
      (fact "should return a subscription"
        (let [user (actions.user/create (factory User))
              subscribee (actions.user/create (factory User))]
          (model.subscription/drop!)
          (session/with-user user
            (let [request {:params {:subscribeto (str (:_id user))}
                           :serialization :http}]
              (filter-action #'subscribe request) => model/subscription?))))))

  (deftest filter-action-test "#'get-subscribers :xmpp"
    (testing "when there are subscribers"
      (fact "should not be empty"
        (let [user (actions.user/create (factory User))
              subscriber (actions.user/create (factory User))
              request (-> (model.subscription/subscribers-request
                           user subscriber)
                          packet/make-request
                          (assoc :serialization :xmpp))]
          (session/with-user subscriber (subscribe user))
          (let [[user subscribers] (filter-action #'get-subscribers request)]
            subscribers => seq
            subscribers => (partial every? model/subscription?))))))

  (deftest filter-action-test "#'get-subscriptions :xmpp"
    (testing "when there are subscriptions"
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
            subscriptions => seq
            subscriptions => (partial every? model/subscription?)))))))
