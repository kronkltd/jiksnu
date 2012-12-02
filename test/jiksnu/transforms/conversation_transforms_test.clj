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
