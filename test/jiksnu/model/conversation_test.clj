(ns jiksnu.model.conversation-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :refer [count-records create delete drop!
                                               fetch-all fetch-by-id]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [validateur.validation :refer [valid?]])
  (:import jiksnu.model.Conversation))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "#'count-records"
  (fact "when there aren't any items"
    (drop!)
    (count-records) => 0)
  (fact "when there are items"
    (drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-conversation-exists))
      (count-records) => n)))

(facts "#'delete"
  (let [item (mock/a-conversation-exists)]
    (delete item) => item
    (fetch-by-id (:_id item)) => nil))

(facts "#'drop!"
  (dotimes [i 1]
    (mock/a-conversation-exists))
  (drop!)
  (count-records) => 0)

(facts "#'fetch-by-id"
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-conversation-exists)]
      (fetch-by-id (:_id item)) => item)))

(facts "#'create"
  (fact "when given valid params"
    (let [domain (mock/a-domain-exists)
          source (mock/a-feed-source-exists)
          params (actions.conversation/prepare-create
                  (factory :conversation {:update-source (:_id source)
                                          :local false
                                          :domain (:_id domain)}))]
      (create params) => (partial instance? Conversation)))

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
        (mock/a-conversation-exists))

      (fetch-all) => #(= (count %) 20)

      (fetch-all {} {:page 2}) => #(= (count %) (- n 20)))))
