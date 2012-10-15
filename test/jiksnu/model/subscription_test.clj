(ns jiksnu.model.subscription-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.subscription :only [delete drop! create fetch-all
                                          fetch-by-id subscribe subscribing?
                                          subscribed?]]
        [midje.sweet :only [fact => future-fact every-checker truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User
           jiksnu.model.Subscription))

(test-environment-fixture

 (fact "#'delete"
   (drop!)
   (let [subscription (create (factory :subscription))]
     (delete subscription)
     (fetch-by-id (:_id subscription)) => nil))

 (fact "#'drop!"
   (fact "when there are subscriptions"
     (fact "should delete them all"
       (let [actor (existance/a-user-exists)
             user (existance/a-user-exists)]
         (subscribe (:_id actor) (:_id user))
         (drop!)
         (fetch-all) => empty?))))

 (fact "#'fetch-all"
   (fact "when there are no subscriptions"
     (fact "should return an empty sequence"
       (fetch-all) =>
       (every-checker
        empty?
        seq?)))
   (fact "when there are subscriptions"
     (dotimes [i 30]
       (create (factory :subscription)))
     (fact "should return a sequence of subscriptions"
       (fetch-all) =>
       (every-checker
        (fn [s] (fact (count s) => 20))
        (fn [s] (every? (partial instance? Subscription) s))))))

 (fact "#'subscribe"

   (fact "when the user is logged in"
     (fact "and the subscription doesn't exist"
       (fact "should return a Subscription"
         (drop!)
         (let [actor (existance/a-user-exists)
               user (existance/a-user-exists)]
           (subscribe (:_id actor) (:_id user)) =>
           (every-checker
            truthy
            (partial instance? Subscription)))))))

 (fact "#'subscribing?"

   (fact "when the user is subscribing"
     (fact "should return true"
       (let [actor (existance/a-user-exists)
             user (existance/a-user-exists)]
         (subscribe actor user)
         (let [response (subscribing? actor user)]
           response => truthy))))

   (fact "when the user is not subscribed"
     (fact "should return a false value"
       (let [actor (existance/a-user-exists)
             user (existance/a-user-exists)]
         (let [response (subscribing? actor user)]
           response =not=> truthy)))))

 (fact "#'subscribed?"

   (fact "when the user is subscribed"
     (fact "should return true"
       (let [actor (existance/a-user-exists)
             user (existance/a-user-exists)]
         (subscribe user actor)
         (let [response (subscribed? actor user)]
           response => truthy))))

   (fact "when the user is not subscribed"
     (fact "should return a false value"
       (let [actor (existance/a-user-exists)
             user (existance/a-user-exists)]
         (let [response (subscribed? actor user)]
           response =not=> truthy)))))
)
