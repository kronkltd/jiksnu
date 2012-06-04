(ns jiksnu.actions.conversation-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.conversation-actions :only [index create delete show]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.conversation :as model.conversation]))

(test-environment-fixture

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
