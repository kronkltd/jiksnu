(ns jiksnu.actions.stream-actions-test
  (:require [ciste.sections.default :refer [index-section]]
            [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import org.bson.types.ObjectId))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "#'actions.stream/public-timeline"
  (fact "when there are no activities"
    (db/drop-all!)
    (actions.stream/public-timeline) =>
    (contains
     {:totalItems 0
      :items empty?}))
  (fact "when there are activities"
    (db/drop-all!)
    (let [activity (mock/there-is-an-activity)]
      (actions.stream/public-timeline) =>
      (contains
       {:totalItems 1
        :items (has every? #(instance? ObjectId %))}))))

(fact "#'actions.stream/user-timeline"
  (fact "when the user has activities"
    (let [user (mock/a-user-exists)
          activity (mock/there-is-an-activity)]
      (actions.stream/user-timeline user) =>
      (just
       user
       (contains
        {:totalItems 1
         :items (has every? #(instance? ObjectId %))})))))
