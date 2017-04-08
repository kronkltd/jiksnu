(ns jiksnu.model.feed-subscription-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model.feed-subscription :refer [create count-records delete
                                                    drop! fetch-all fetch-by-id]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(th/module-test ["jiksnu.modules.core"])

(facts "#'count-records"
  (fact "when there aren't any items"
    (drop!)
    (count-records) => 0)
  (fact "when there are items"
    (drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-feed-subscription-exists))
      (count-records) => n)))

(facts "#'delete"
  (let [item (mock/a-feed-subscription-exists)]
    (delete item) => item
    (fetch-by-id (:_id item)) => nil))

(facts "#'drop!"
  (dotimes [i 1]
    (mock/a-feed-subscription-exists))
  (drop!)
  (count-records) => 0)

(facts "#'fetch-by-id"
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-feed-subscription-exists)]
      (fetch-by-id (:_id item)) => item)))

(facts "#'create"
  (fact "when given valid params"
    (let [params (actions.feed-subscription/prepare-create
                  (factory :feed-subscription {:local false}))]
      (create params) =>
      (every-checker
       (partial instance? FeedSubscription)
       (contains
        {:_id     (partial instance? ObjectId)
         :created (partial instance? DateTime)
         :updated (partial instance? DateTime)
         :url     string?}))))

  (fact "when given invalid params"
    (create {}) => (throws RuntimeException)))

(facts "#'fetch-all"
  (fact "when there are no items"
    (drop!)
    (fetch-all) => empty?)

  (fact "when there is more than a page of items"
    (drop!)

    (let [n 25]
      (dotimes [i n]
        (mock/a-feed-subscription-exists))

      (fetch-all) => #(= (count %) 20)
      (fetch-all {} {:page 2}) => #(= (count %) (- n 20)))))
