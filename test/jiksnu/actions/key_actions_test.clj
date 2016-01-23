(ns jiksnu.actions.key-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'actions.key/index
  (actions.key/index) => map?)
