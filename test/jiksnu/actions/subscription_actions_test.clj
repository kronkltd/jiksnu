(ns jiksnu.actions.subscription-actions-test
  (:use (ciste [config :only [with-environment]])
        [clj-factory.core :only [factory]]
        clojure.test
        jiksnu.actions.subscription-actions
        (jiksnu test-helper model
                [session :only (with-user)])
        midje.sweet)
  (:require (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))


(test-environment-fixture

  (against-background
    [(around :facts
             (let [user (model.user/create (factory User))] ?form))]
    
    (fact "subscribe"
      (fact "when the user is not already subscribed"
       (fact "should return a subscription"
         (let [subscribee (model.user/create (factory User))]
           (model.subscription/drop!)
           (with-user user
             (subscribe user subscribee) => subscription?)))))
    
    (fact "subscribed"
      (fact "should return a subscription"
       (let [subscribee (model.user/create (factory User))]
         (subscribed user subscribee) => subscription?)))

    (fact "subscribers"
      (fact "when there are subscribers"
      (fact "should not be empty"
        (let [subscriber (model.user/create (factory User))
              subscription (model.subscription/create
                            (factory Subscription
                                     {:from (:_id subscriber)
                                      :to (:_id user)}))
              [_ subscriptions] (get-subscribers user)]
          subscriptions => seq?
          subscriptions => (partial every? (partial instance? Subscription))))))

    (fact "get-subscriptions"
      (fact "when there are subscriptions"
      (fact "should return a sequence of subscriptions"
        (let [subscribee (model.user/create (factory User))
              subscription (model.subscription/create
                            (factory Subscription
                                     {:from (:_id user)
                                      :to (:_id subscribee)}))
              [user subscriptions] (get-subscriptions user)]
          subscriptions => (comp not empty?)
          subscriptions => (partial every? (partial instance? Subscription))))))))
