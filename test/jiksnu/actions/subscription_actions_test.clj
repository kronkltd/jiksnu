(ns jiksnu.actions.subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        jiksnu.actions.subscription-actions
        [jiksnu.mock :as mock]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))


(test-environment-fixture

 (context "subscribe"
   (context "when the user is not already subscribed"
     (context "should return a subscription"
       (let [user (mock/a-user-exists)
             subscribee (mock/a-user-exists)]
         (model.subscription/drop!)
         (with-user user
           (subscribe user subscribee) => (partial instance? Subscription))))))

 (context "subscribed"
   (context "should return a subscription"
     (let [user (mock/a-user-exists)
           subscribee (mock/a-user-exists)]
       (subscribed user subscribee) => (partial instance? Subscription))))

 (context "get-subscribers"
   (context "when there are subscribers"
     (let [subscription (mock/a-subscription-exists)
           target (model.subscription/get-target subscription)]
       (get-subscribers target) =>
       (check [[_ {:keys [items]} :as response]]
         response => vector?
         (first response) => (partial instance? User)
         (doseq [subscription items]
           subscription => (partial instance? Subscription))))))

 (context "get-subscriptions"
   (context "when there are subscriptions"
     (let [subscription (mock/a-subscription-exists)
           actor (model.subscription/get-actor subscription)]
       (get-subscriptions actor) =>
       (check [response]
         response => vector?
         (first response) => actor
         (let [subscriptions (second response)]
           subscriptions =>  map?
           (:items subscriptions) =>
           (partial every? (partial instance? Subscription)))))))
 )
