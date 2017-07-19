(ns jiksnu.modules.core.model.conversation-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.model.conversation :as model.conversation]
            [jiksnu.modules.core.actions.conversation-actions :as actions.conversation]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.modules.core.model.Conversation))

(th/module-test ["jiksnu.modules.core"])

(facts "#'jiksnu.modules.core.model.conversation/count-records"
  (fact "when there aren't any items"
    (model.conversation/drop!)
    (model.conversation/count-records) => 0)
  (fact "when there are items"
    (model.conversation/drop!)
    (let [n 15]
      (dotimes [_ n] (mock/a-conversation-exists))
      (model.conversation/count-records) => n)))

(facts "#'jiksnu.modules.core.model.conversation/delete"
  (let [item (mock/a-conversation-exists)]
    (model.conversation/delete item) => item
    (model.conversation/fetch-by-id (:_id item)) => nil))

(facts "#'jiksnu.modules.core.model.conversation/drop!"
  (dotimes [_ 1] (mock/a-conversation-exists))
  (model.conversation/drop!)
  (model.conversation/count-records) => 0)

(facts "#'jiksnu.modules.core.model.conversation/fetch-by-id"
  (fact "when the item doesn't exist"
    (let [id (util/make-id)]
      (model.conversation/fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-conversation-exists)]
      (model.conversation/fetch-by-id (:_id item)) => item)))

(facts "#'jiksnu.modules.core.model.conversation/create"
  (fact "when given valid params"
    (let [domain (mock/a-domain-exists)
          source (mock/a-feed-source-exists)
          params (actions.conversation/prepare-create
                  (factory :conversation {:update-source (:_id source)
                                          :local false
                                          :domain (:_id domain)}))]
      (model.conversation/create params) => (partial instance? Conversation)))

  (fact "when given invalid params"
    (model.conversation/create {}) => (throws RuntimeException)))

(facts "#'jiksnu.modules.core.model.conversation/fetch-all"
  (fact "when there are no items"
    (model.conversation/drop!)
    (model.conversation/fetch-all) => empty?)

  (fact "when there is more than a page of items"
    (model.conversation/drop!)

    (let [n 25]
      (dotimes [_ n] (mock/a-conversation-exists))

      (model.conversation/fetch-all) => #(= (count %) 20)
      (model.conversation/fetch-all {} {:page 2}) => #(= (count %) (- n 20)))))
