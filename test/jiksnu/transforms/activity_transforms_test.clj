(ns jiksnu.transforms.activity-transforms-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.transforms.activity-transforms :refer [set-recipients]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'jiksnu.transforms.activity-transforms/set-recipients"

  (fact " when there are no recipient uris"
    (let [activity (factory :activity)]
      (set-recipients activity) => activity))

  (fact "When the activity contains a recipient uri"
    (let [recipient (mock/a-user-exists)
          activity (factory :activity {:recipient-uris [(:_id recipient)]})]
      (set-recipients activity) => (contains {:recipients (contains (:_id recipient))}))))
