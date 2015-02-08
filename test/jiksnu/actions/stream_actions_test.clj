(ns jiksnu.actions.stream-actions-test
  (:require [ciste.sections.default :refer [index-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "#'actions.stream/public-timeline"
  (fact "when there are no activities"
    (model.activity/drop!)
    (actions.stream/public-timeline) =>
    (contains
     {:totalItems 0
      :items empty?})

(comp empty? :items))
  (fact "when there are activities"
    (let [activity (mock/there-is-an-activity)]
      (actions.stream/public-timeline) =>
      (contains
       {:totalItems 1
        :items (has every? #(instance? Conversation %))}))))

(fact "#'actions.stream/user-timeline"
  (fact "when the user has activities"
    (let [user (mock/a-user-exists)
          activity (mock/there-is-an-activity)]
      (actions.stream/user-timeline user) =>
      (just
       user
       (contains
        {:totalItems 1
         :items (has every? #(instance? Activity %))})))))
