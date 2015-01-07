(ns jiksnu.model.feed-subscription-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :refer [create count-records delete
                                                    drop! fetch-all fetch-by-id]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'count-records
  (fact "when there aren't any items"
    (drop!)
    (count-records) => 0)
  (fact "when there are items"
    (drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-feed-subscription-exists))
      (count-records) => n)))

(fact #'delete
  (let [item (mock/a-feed-subscription-exists)]
    (delete item) => item
    (fetch-by-id (:_id item)) => nil))

(fact #'drop!
  (dotimes [i 1]
    (mock/a-feed-subscription-exists))
  (drop!)
  (count-records) => 0)

(fact #'fetch-by-id
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-feed-subscription-exists)]
      (fetch-by-id (:_id item)) => item)))

(fact #'create
  (fact "when given valid params"
    (let [params (actions.feed-subscription/prepare-create
                  (factory :feed-subscription {:local false}))]
      (create params)) =>
      (th/check [response]
             response => map?
             response => (partial instance? FeedSubscription)
             (:_id response) =>  (partial instance? ObjectId)
             (:created response) => (partial instance? DateTime)
             (:updated response) => (partial instance? DateTime)
             (:url response) => string?))

  (fact "when given invalid params"
    (create {}) => (throws RuntimeException)))

(fact #'fetch-all
  (fact "when there are no items"
    (drop!)
    (fetch-all) => empty?)

  (fact "when there is more than a page of items"
    (drop!)

    (let [n 25]
      (dotimes [i n]
        (mock/a-feed-subscription-exists))

      (fetch-all) =>
      (th/check [response]
             response => seq?
             (count response) => 20)

      (fetch-all {} {:page 2}) =>
      (th/check [response]
             response => seq?
             (count response) => (- n 20)))))



