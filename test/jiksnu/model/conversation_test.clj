(ns jiksnu.model.conversation-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.conversation :only [count-records create delete drop! fetch-all fetch-by-id]]
        [midje.sweet :only [fact future-fact => throws every-checker]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.util :as util])
  (:import jiksnu.model.Conversation))

(test-environment-fixture

 (fact "#'fetch-by-id"
   (fact "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the item exists"
     (let [item (mock/a-conversation-exists)]
       (fetch-by-id (:_id item)) => item)))

 (fact "#'create"
   (fact "when given valid params"
     (let [domain (mock/a-domain-exists)
           source (mock/a-feed-source-exists)
           params (actions.conversation/prepare-create
                   (factory :conversation {:update-source (:_id source)
                                           :domain (:_id domain)}))]
       (create params) => (partial instance? Conversation)))

   (fact "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (fact "#'drop!"
   (dotimes [i 1]
     (mock/a-conversation-exists))
   (drop!)
   (count-records) => 0)

 (fact "#'delete"
   (let [item (mock/a-conversation-exists)]
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
         (mock/a-conversation-exists))

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
         (mock/a-conversation-exists))
       (count-records) => n)))

 )
