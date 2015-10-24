(ns jiksnu.modules.admin.actions.feed-source-actions-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.modules.admin.actions.feed-source-actions :refer [index]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'index"
  (fact "when there are no sources"
    (:items (index)) => empty?)

  (fact "when there are many sources"
    (dotimes [i 25]
      (mock/a-feed-source-exists))

    ;; TODO: hardcoded configurable value
    (count (:items (index))) => 20))
