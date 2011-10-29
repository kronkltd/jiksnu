(ns jiksnu.actions.subscription-actions-test
  (:use [clj-factory.core :only (factory)]
        clojure.test
        jiksnu.actions.subscription-actions
        (jiksnu core-test model
                [session :only (with-user)])
        midje.sweet)
  (:require (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest test-subscribe
  (testing "when the user is not already subscribed"
    (fact "should return a subscription"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))]
        (model.subscription/drop!)
        (with-user user
          (subscribe user subscribee) => subscription?)))))

(deftest test-subscribed
  (fact "should return a subscription"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))]
      (subscribed user subscribee) => subscription?)))

(deftest test-subscribers
  (testing "when there are subscribers"
    (fact "should not be empty"
      (let [user (model.user/create (factory User))
            subscriber (model.user/create (factory User))
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id subscriber)
                                    :to (:_id user)}))
            [_ subscriptions] (subscribers user)]
        subscriptions => seq?
        subscriptions => (partial every? (partial instance? Subscription))))))

(deftest test-subscriptions
  (testing "when there are subscriptions"
    (fact "should return a sequence of subscriptions"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id user)
                                    :to (:_id subscribee)}))
            [user subscriptions] (subscriptions user)]
        subscriptions => (comp not empty?)
        subscriptions => (partial every? (partial instance? Subscription))))))
