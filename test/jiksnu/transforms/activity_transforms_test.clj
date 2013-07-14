(ns jiksnu.transforms.activity-transforms-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.transforms.activity-transforms :only [set-recipients]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [midje.sweet :only [=> throws truthy falsey]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]))

(test-environment-fixture

 (context #'set-recipients
   (context "when there are no recipient uris"
     (context "should return that activity"
       (let [activity (factory :activity)]
         (set-recipients activity) => activity)))
   (context "When the activity contains a recipient uri"
     (let [recipient (mock/a-user-exists)
           activity (factory :activity {:recipient-uris [(:id recipient)]})]
       (set-recipients activity) =>
       (check [response]
         (first (:recipients response)) => (:_id recipient)))))

 )

