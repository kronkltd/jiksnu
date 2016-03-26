(ns jiksnu.model.subscription-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Subscription))

(th/module-test ["jiksnu.modules.core"])

(facts "#'model.subscription/count-records"
  (fact "when there aren't any items"
    (model.subscription/drop!)
    (model.subscription/count-records) => 0)
  (fact "when there are items"
    (model.subscription/drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-subscription-exists))
      (model.subscription/count-records) => n)))

(facts "#'model.subscription/delete"
  (let [item (mock/a-subscription-exists)]
    (model.subscription/delete item) => item
    (model.subscription/fetch-by-id (:_id item)) => nil))

(facts "#'model.subscription/drop!"
  (dotimes [i 1]
    (mock/a-subscription-exists))
  (model.subscription/drop!)
  (model.subscription/count-records) => 0)

(facts "#'model.subscription/fetch-by-id"
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (model.subscription/fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-subscription-exists)]
      (model.subscription/fetch-by-id (:_id item)) => item)))

(facts "#'model.subscription/create"
  (fact "when given valid params"
    (let [params (actions.subscription/prepare-create
                  (factory :subscription))]
      (model.subscription/create params) => (partial instance? Subscription)))

  (fact "when given invalid params"
    (model.subscription/create {}) => (throws RuntimeException)))

(facts "#'model.subscription/fetch-all"
  (fact "when there are no items"
    (model.subscription/drop!)
    (model.subscription/fetch-all) => empty?)

  (fact "when there is more than a page of items"
    (model.subscription/drop!)

    (let [n 25]
      (dotimes [i n]
        (mock/a-subscription-exists))

      (let [response (model.subscription/fetch-all)]
        response => seq?
        (count response) => 20)

      (let [response (model.subscription/fetch-all {} {:page 2})]
        response => seq?
        (count response) => (- n 20)))))

(facts "#'model.subscription/subscribing?"

  (fact "when the user is subscribing"
    (let [subscription (mock/a-subscription-exists)
          actor (model.subscription/get-actor subscription)
          target (model.subscription/get-target subscription)]
      (model.subscription/subscribing? actor target) => true))

  (fact "when the user is not subscribed"
    (let [actor (mock/a-user-exists)
          target (mock/a-user-exists)]

      (model.subscription/subscribing? actor target) => false)))

(facts "#'model.subscription/subscribed?"

  (fact "when the user is subscribed"
    (let [subscription (mock/a-subscription-exists)
          ;; NB: We're reversing these because we want to check the reverse
          target (model.subscription/get-actor subscription)
          actor (model.subscription/get-target subscription)]
      (model.subscription/subscribed? actor target) => true))

  (fact "when the user is not subscribed"
    (let [actor (mock/a-user-exists)
          target (mock/a-user-exists)]
      (model.subscription/subscribed? actor target) => false)))
