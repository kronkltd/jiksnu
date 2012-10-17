(ns jiksnu.actions.conversation-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.conversation-actions :only [index create delete set-local
                                                    show]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains fact]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model.conversation :as model.conversation]))

(test-environment-fixture

 (fact "#'set-local"
   (fact "when the local flag is already set"
     (let [conversation-1 (factory :conversation {:local true})
           conversation-2 (factory :conversation {:local false})]
       (set-local conversation-1) => conversation-1
       (set-local conversation-2) => conversation-2))
   (fact "when the local flag is not set"
     (fact "when the url is local"
       (let [domain (actions.domain/current-domain)
             url (make-uri (:_id domain))
             conversation (factory :conversation {:url url})]
         (set-local conversation) => (contains {:local true})))
     (fact "when the url is not local"
       (let [domain (existance/a-record-exists :domain)
             url (make-uri (:_id domain))
             conversation (factory :conversation {:url url})]
         (set-local conversation) => (contains {:local false})))))

 (fact "#'index"
   (fact "should return a page structure"
     (index) => map?))

 (fact "#'create"
   (create (factory :conversation)) => map?)

 (fact "#'delete"
   (let [conversation (create (factory :conversation))]
     (delete conversation) => map?
     (model.conversation/fetch-by-id (:_id conversation)) => nil))

 (fact "#'show"
   (show .conversation.) => .conversation.)
 
 )
