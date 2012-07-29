(ns jiksnu.actions.subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        jiksnu.actions.subscription-actions
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.model
        [jiksnu.session :only [with-user]]
        midje.sweet)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))


(test-environment-fixture

 (fact "subscribe"
   (fact "when the user is not already subscribed"
     (fact "should return a subscription"
       (let [user (model.user/create (factory :local-user))
             subscribee (model.user/create (factory :local-user))]
         (model.subscription/drop!)
         (with-user user
           (subscribe user subscribee) => subscription?)))))

 (fact "subscribed"
   (fact "should return a subscription"
     (let [user (model.user/create (factory :local-user))
           subscribee (model.user/create (factory :local-user))]
       (subscribed user subscribee) => subscription?)))

 (fact "get-subscribers"
   (fact "when there are subscribers"
     (fact "should not be empty"
       (let [user (model.user/create (factory :local-user))
             subscriber (model.user/create (factory :local-user))
             subscription (model.subscription/create
                           (factory :subscription
                                    {:from (:_id subscriber)
                                     :to (:_id user)}))]
         (get-subscribers user) =>
         (every-checker
          vector?
          (comp (partial instance? User) first)
          (fn [[_ {:keys [items] :as page}]]
            (fact
              (doseq [subscription items]
                subscription => (partial instance? Subscription)))))))))

 (fact "get-subscriptions"
   (fact "when there are subscriptions"
     (fact "should return a sequence of subscriptions"
       (let [user (model.user/create (factory :local-user))
             subscribee (model.user/create (factory :local-user))
             subscription (model.subscription/create
                           (factory :subscription
                                    {:from (:_id user)
                                     :to (:_id subscribee)}))]
         (get-subscriptions user) =>
         (every-checker
          vector?
          #(= user (first %))
          (fn [response]
            (let [subscriptions (second response)]
              (fact
                subscriptions =>  map?
                (:items subscriptions) =>
                (partial every? (partial instance? Subscription))))))))))
 )
