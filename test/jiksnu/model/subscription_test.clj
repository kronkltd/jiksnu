(ns jiksnu.model.subscription-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.subscription :only [delete drop! create count-records fetch-all
                                          fetch-by-id subscribe subscribing?
                                          subscribed?]]
        [midje.sweet :only [fact => future-fact every-checker throws truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User
           jiksnu.model.Subscription))

(test-environment-fixture

 (fact "#'fetch-by-id"
   (fact "when the item doesn't exist"
     (let [id (model/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the item exists"
     (let [item (existance/a-subscription-exists)]
      (fetch-by-id (:_id item)) => item)))

 (fact "#'create"
   (fact "when given valid params"
     (let [params (actions.subscription/prepare-create
                   (factory :subscription))]
       (create params) => (partial instance? Subscription)))

   (fact "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (fact "#'delete"
   (let [item (existance/a-subscription-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

 (fact "#'drop!"
   (dotimes [i 1]
     (existance/a-subscription-exists))
   (drop!)
   (count-records) => 0)

 (fact "#'fetch-all"
   (fact "when there are no items"
     (drop!)
     (fetch-all) => (every-checker
      seq?
      empty?))

   (fact "when there is more than a page of items"
     (drop!)

     (let [n 25]
       (dotimes [i n]
         (existance/a-subscription-exists))

       (fetch-all) =>
       (every-checker
        seq?
        #(fact (count %) => 20))

       (fetch-all {} {:page 2}) =>
       (every-checker
        seq?
        #(fact (count %) => (- n 20))))))

 (fact "#'count-records"
   (fact "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (fact "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (existance/a-subscription-exists))
       (count-records) => n)))
 
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
