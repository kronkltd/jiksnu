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
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> fact truthy]])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

(test-environment-fixture

 (fact #'actions.stream/public-timeline
   (fact "when there are no activities"
     (fact "should be empty"
       (model.activity/drop!)
       (actions.stream/public-timeline) => (comp empty? :items)))
   (fact "when there are activities"
     (fact "should return a seq of activities"
       (let [activity (mock/there-is-an-activity)]
         (actions.stream/public-timeline) =>
         (check [response]
                response => map?
                (:totalRecords response) => 1
                (let [items (:items response)]
                  items => seq?
                  (doseq [item items]
                    (class item) => Conversation)))))))

 (fact #'actions.stream/user-timeline
   (fact "when the user has activities"
     (db/drop-all!)
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity)]
       (actions.stream/user-timeline user) =>
       (check [response]
              response => vector?
              (first response) => user
              (second response) => map?
              (:totalRecords (second response)) => 1))))

 )

