(ns jiksnu.model.feed-subscription-test
    (:use [clj-factory.core :only [factory]]
          [jiksnu.test-helper :only [test-environment-fixture]]
          [jiksnu.model.feed-subscription :only [create fetch-all fetch-by-id]]
          [midje.sweet :only [every-checker fact future-fact throws =>]])
    (:require [clojure.tools.logging :as log]
              [jiksnu.model :as model])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime
           slingshot.ExceptionInfo))

(test-environment-fixture

 (fact "#'fetch-by-id"
   (fact "when the record doesn't exist"
     (let [id (model/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the record exists"
     (let [record (create (factory :feed-subscription))]
       (fetch-by-id (:_id record)) => record)))
 
 (fact "#'create"
   (fact "when given valid params"
     (create (factory :feed-subscription)) =>
     (every-checker
      map?
      (partial instance? FeedSubscription)
      #(instance? ObjectId (:_id %))
      #(instance? DateTime (:created %))
      #(instance? DateTime (:updated %))
      #(string? (:topic %))))

   (fact "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (fact "#'fetch-all"
   (fact "when there are no records"
     (model/drop-all!)
     
     (fetch-all) =>
     (every-checker
      seq?
      empty?))

   (fact "when there is more than a page"
     (model/drop-all!)

     (dotimes [n 25]
       (create (factory :feed-subscription)))

     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 20))

     (fetch-all {} {:page 2}) =>
     (every-checker
      seq?
      #(fact (count %) => 5))))

 )

