(ns jiksnu.modules.admin.actions.feed-source-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.mock :as mock]
            [jiksnu.modules.admin.actions.feed-source-actions :refer [index]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact #'index
   (fact "when there are no sources"
     (:items (index)) => empty?)

   (fact "when there are many sources"
     (dotimes [i 25]
       (mock/a-feed-source-exists))

     ;; TODO: hardcoded configurable value
     (count (:items (index))) => 20))

 )
