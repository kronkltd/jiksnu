(ns jiksnu.model.feed-subscription-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.feed-subscription :only [create count-records delete drop!
                                               fetch-all fetch-by-id]]
        [midje.sweet :only [every-checker fact future-fact throws =>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.util :as util])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime
           slingshot.ExceptionInfo))

(test-environment-fixture

 (fact "#'fetch-by-id"
   (fact "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the item exists"
     (let [item (mock/a-feed-subscription-exists)]
       (fetch-by-id (:_id item)) => item)))

 (fact "#'create"
   (fact "when given valid params"
     (let [params (actions.feed-subscription/prepare-create
                   (factory :feed-subscription {:local false}))]
       (create params)) =>
       (every-checker
        map?
        (partial instance? FeedSubscription)
        #(instance? ObjectId (:_id %))
        #(instance? DateTime (:created %))
        #(instance? DateTime (:updated %))
        #(string? (:url %))))

   (fact "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (fact "#'drop!"
   (dotimes [i 1]
     (mock/a-feed-subscription-exists))
   (drop!)
   (count-records) => 0)

 (fact "#'delete"
   (let [item (mock/a-feed-subscription-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

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
         (mock/a-feed-subscription-exists))

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
         (mock/a-feed-subscription-exists))
       (count-records) => n)))

 )

