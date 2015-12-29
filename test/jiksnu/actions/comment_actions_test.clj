(ns jiksnu.actions.comment-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.comment-actions :refer :all]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'fetch-comments
  (fact "when the activity exists"
    (fact "and there are no comments"
      (let [actor (mock/a-user-exists)
            activity (mock/there-is-an-activity)
            [_ comments] (fetch-comments activity)]
        comments => empty?))))
