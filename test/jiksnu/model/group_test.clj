(ns jiksnu.model.group-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'model.group/delete
  (fact "when the item exists"
    (let [item (mock/a-group-exists)]
      (model.group/delete item) =>  item)))

(fact #'model.group/fetch-by-id
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (model.group/fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-group-exists)]
      (model.group/fetch-by-id (:_id item)) => item)))

(fact #'model.group/fetch-all
  (fact "when there are no items"
    (db/drop-all!)
    (model.group/fetch-all) => empty?)

  (fact "when there are less than a page"
    (db/drop-all!)
    (dotimes [n 19]
      (actions.group/create (factory :group)))
    (let [response (model.group/fetch-all)]
      response => seq?
      (count response) => 19))

  (fact "when there is more than a page"
    (db/drop-all!)
    (dotimes [n 21]
      (actions.group/create (factory :group)))
    (let [response (model.group/fetch-all)]
      response => seq?
      (count response) => 20)))


