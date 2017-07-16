(ns jiksnu.modules.core.actions.stream-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.modules.core.actions.stream-actions :as actions.stream]
            [jiksnu.modules.core.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import org.bson.types.ObjectId))

(th/module-test ["jiksnu.modules.core"])

(facts "#'actions.stream/public-timeline"
  (fact "when there are no activities"
    (db/drop-all!)
    (actions.stream/public-timeline) =>
    (contains
     {:totalItems 0
      :items empty?}))
  (fact "when there are activities"
    (db/drop-all!)
    (let [activity (mock/an-activity-exists)]
      (actions.stream/public-timeline) =>
      (contains
       {:totalItems 1
        :items (has every? #(instance? ObjectId %))}))))

(fact "#'actions.stream/user-timeline"
  (fact "when the user has activities"
    (let [user (mock/a-user-exists)
          activity (mock/an-activity-exists)]
      (actions.stream/user-timeline user) =>
      (just
       user
       (contains
        {:totalItems 1
         :items (has every? #(instance? ObjectId %))})))))
