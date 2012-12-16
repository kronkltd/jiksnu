(ns jiksnu.actions.admin.feed-source-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.admin.feed-source-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]])
  (:require [jiksnu.mock :as mock]
            [jiksnu.model.feed-source :as model.feed-source]))

(test-environment-fixture

 (fact "#'index"
   (fact "when there are no sources"
     (:items (index)) => empty?)

   (fact "when there are many sources"
     (dotimes [i 25]
       (mock/a-feed-source-exists))

     ;; TODO: hardcoded configurable value
     (count (:items (index))) => 20))

 )
