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

(fact "#'set-recipients"

  (fact "when there are no recipient uris"
    (fact "should return that activity"
      (let [activity (factory :activity)]
        (set-recipients activity) => activity)))

  (future-fact "When the activity contains a recipient uri"
    (let [recipient (mock/a-user-exists)
          activity (factory :activity {:recipient-uris [(:_id recipient)]})]
      (set-recipients activity) => (contains {:recipients (:_id recipient)})))
  )


