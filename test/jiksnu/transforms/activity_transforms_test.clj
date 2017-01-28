(ns jiksnu.transforms.activity-transforms-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.mock :as mock]
            [jiksnu.transforms.activity-transforms :refer [set-recipients set-streams]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import org.bson.types.ObjectId))

(th/module-test ["jiksnu.modules.core"])

(facts "#'jiksnu.transforms.activity-transforms/set-recipients"
  (fact " when there are no recipient uris"
    (let [activity (factory :activity)]
      (set-recipients activity) => activity))

  (fact "When the activity contains a recipient uri"
    (let [recipient (mock/a-user-exists)
          activity (factory :activity {:recipient-uris [(:_id recipient)]})]
      (set-recipients activity) => (contains {:recipients (contains (:_id recipient))}))))

(facts "#'jiksnu.transforms.activity-transforms/set-streams"
  (fact "no streams"
    (set-streams {}) => {})
  (fact "nil streams"
    (set-streams {:streams nil}) => {})
  #_
  (fact "empty string"
    (set-streams {:streams ""}) => {})
  #_
  (fact "single id"
    (set-streams {:streams ["4"]}) => (contains {:streams [(ObjectId. "4")]})
    )
  )
