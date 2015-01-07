(ns jiksnu.model.resource-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.resource :refer [count-records create delete drop!
                                           fetch-all fetch-by-id]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [validateur.validation :refer [valid?]])
  (:import jiksnu.model.Resource))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'count-records
  (fact "when there aren't any items"
    (drop!)
    (count-records) => 0)
  (fact "when there are conversations"
    (drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-resource-exists))
      (count-records) => n)))

(fact #'delete
  (let [item (mock/a-resource-exists)]
    (delete item) => item
    (fetch-by-id (:_id item)) => nil))

(fact #'fetch-by-id
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-resource-exists)]
      (fetch-by-id (:_id item)) => item)))

(fact #'create
  (fact "when given valid params"
    (let [params (actions.resource/prepare-create
                  (factory :resource))]
      (create params) => (partial instance? Resource)))

  (fact "when given invalid params"
    (create {}) => (throws RuntimeException)))

(fact #'fetch-all
  (fact "when there are no records"
    (drop!)
    (fetch-all) => empty?)

  (fact "when there is more than a page"
    (drop!)

    (dotimes [n 25]
      (mock/a-resource-exists))

    (fetch-all) =>
    (check [response]
           response => seq?
           (count response) => 20)

    (fetch-all {} {:page 2}) =>
    (check [response]
           response => seq?
           (count response) => 5)))


