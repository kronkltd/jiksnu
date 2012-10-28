(ns jiksnu.actions.conversation-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.conversation-actions :only [create delete index show]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains fact]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model.conversation :as model.conversation]))

(test-environment-fixture

 (fact "#'index"
   (fact "should return a page structure"
     (index) => map?))

 (fact "#'create"
   (let [domain (existance/a-domain-exists)
         domain-name (:_id domain)
         url (make-uri domain-name)
         source (existance/a-feed-source-exists {:domain domain
                                                 :url url})
         params (factory :conversation {:domain domain-name
                                        :url url
                                        :update-source source})]
     (create params)) => map?)

 (fact "#'delete"
   (let [conversation (existance/a-conversation-exists)]
     (delete conversation) => map?
     (model.conversation/fetch-by-id (:_id conversation)) => nil))

 (fact "#'show"
   (show .conversation.) => .conversation.)
 
 )
