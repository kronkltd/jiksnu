(ns jiksnu.actions.subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        jiksnu.actions.subscription-actions
        [jiksnu.mock :as mock]
        [jiksnu.test-helper :only [context test-environment-fixture]]
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

 (context "subscribe"
   (context "when the user is not already subscribed"
     (context "should return a subscription"
       (let [user (mock/a-user-exists)
             subscribee (mock/a-user-exists)]
         (model.subscription/drop!)
         (with-user user
           (subscribe user subscribee) => subscription?)))))

 (context "subscribed"
   (context "should return a subscription"
     (let [user (mock/a-user-exists)
           subscribee (mock/a-user-exists)]
       (subscribed user subscribee) => subscription?)))

 (context "get-subscribers"
   (context "when there are subscribers"
     (let [subscription (mock/a-subscription-exists)
           target (model.subscription/get-target subscription)]
       (get-subscribers target) =>
       (every-checker
        vector?
        (comp (partial instance? User) first)
        (fn [[_ {:keys [items] :as page}]]
          (fact
            (doseq [subscription items]
              subscription => (partial instance? Subscription))))))))

 (context "get-subscriptions"
   (context "when there are subscriptions"
     (let [subscription (mock/a-subscription-exists)
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
