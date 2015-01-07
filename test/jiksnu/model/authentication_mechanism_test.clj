(ns jiksnu.model.authentication-mechanism-test
  (:use [jiksnu.model.like :only [fetch-all]]
        [jiksnu.test-helper :as th]
        [midje.sweet :only :all]))


(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'fetch-all
  (fact "when not given any parameters"
    (fetch-all) => seq?)

  (fact "when passed an empty map"
    (fetch-all {}) => seq?))

