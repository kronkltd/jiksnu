(ns jiksnu.model.conversation-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context test-environment-fixture]]
        [jiksnu.model.conversation :only [count-records create delete drop! fetch-all fetch-by-id]]
        [midje.sweet :only [=> fact throws]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.util :as util])
  (:import jiksnu.model.Conversation))

(test-environment-fixture

 (fact #'count-records
   (fact "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (fact "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/a-conversation-exists))
       (count-records) => n)))

 (fact #'delete
   (let [item (mock/a-conversation-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

 (fact #'drop!
   (dotimes [i 1]
     (mock/a-conversation-exists))
   (drop!)
   (count-records) => 0)

 (fact #'fetch-by-id
   (fact "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the item exists"
     (let [item (mock/a-conversation-exists)]
       (fetch-by-id (:_id item)) => item)))

 (fact #'create
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

 (fact #'fetch-all
   (fact "when there are no items"
     (drop!)
     (fetch-all) =>
     (check [response]
       response => seq?
       response => empty?))

   (fact "when there is more than a page of items"
     (drop!)

     (let [n 25]
       (dotimes [i n]
         (mock/a-conversation-exists))

       (fetch-all) =>
       (check [response]
        response => seq?
        (count response) => 20)

       (fetch-all {} {:page 2}) =>
       (check [response]
         response => seq?
        (count response) => (- n 20)))))

 )
