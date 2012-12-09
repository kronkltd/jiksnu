(ns jiksnu.actions.subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        jiksnu.actions.subscription-actions
        [jiksnu.existance-helpers :as existance]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.model
        [jiksnu.session :only [with-user]]
        midje.sweet)
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))


(test-environment-fixture

 (fact "subscribe"
   (fact "when the user is not already subscribed"
     (fact "should return a subscription"
       (let [user (existance/a-user-exists)
             subscribee (existance/a-user-exists)]
         (model.subscription/drop!)
         (with-user user
           (subscribe user subscribee) => subscription?)))))

 (fact "subscribed"
   (fact "should return a subscription"
     (let [user (existance/a-user-exists)
           subscribee (existance/a-user-exists)]
       (subscribed user subscribee) => subscription?)))

 (fact "get-subscribers"
   (fact "when there are subscribers"
     (let [subscription (existance/a-subscription-exists)
           target (model.subscription/get-target subscription)]
       (get-subscribers target) =>
       (every-checker
        vector?
        (comp (partial instance? User) first)
        (fn [[_ {:keys [items] :as page}]]
          (fact
            (doseq [subscription items]
              subscription => (partial instance? Subscription))))))))

 (fact "get-subscriptions"
   (fact "when there are subscriptions"
     (let [subscription (existance/a-subscription-exists)
           actor (model.subscription/get-actor subscription)]
       (get-subscriptions actor) =>
       (every-checker
        vector?
        #(= actor (first %))
        (fn [response]
          (let [subscriptions (second response)]
            (fact
              subscriptions =>  map?
              (:items subscriptions) =>
              (partial every? (partial instance? Subscription)))))))))
 )
