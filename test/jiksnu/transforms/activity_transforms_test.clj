(ns jiksnu.transforms.activity-transforms-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.transforms.activity-transforms :only [set-recipients]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [midje.sweet :only [fact future-fact => every-checker throws truthy falsey]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]))

(test-environment-fixture

 (fact "#'set-recipients"
   (fact "when there are no recipient uris"
     (fact "should return that activity"
       (let [activity (factory :activity)]
         (set-recipients activity) => activity)))
   (fact "When the activity contains a recipient uri"
     (let [recipient (mock/a-user-exists)
           activity (factory :activity {:recipient-uris [(:id recipient)]})]
       (set-recipients activity) =>
       (every-checker
        #(= (:_id recipient) (first (:recipients %)))))))

 )

