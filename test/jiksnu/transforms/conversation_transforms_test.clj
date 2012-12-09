(ns jiksnu.transforms.conversation-transforms-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.transforms.conversation-transforms :only [set-update-source]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains fact anything]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
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
       (let [domain (existance/a-domain-exists)
             url (make-uri (:_id domain))
             conversation (factory :conversation {:url url})]
         (set-local conversation) => (contains {:local false})))))

 (fact "#'set-update-source"
   (fact "when the update source is set"
     (let [source (existance/a-feed-source-exists)
           conversation (factory :conversation {:update-source (:_id source)})]
       (set-update-source conversation) => conversation))
   (fact "when the update source is not set"
     (fact "and the source can be discovered"
       (let [source (factory :feed-source)
             conversation (-> (factory :conversation)
                              (dissoc :update-source))
             url (:url conversation)]
         (set-update-source conversation) => (contains {:update-source .id.})
         (provided
           (actions.feed-source/discover-source anything) => {:_id .id.})))))

 )
