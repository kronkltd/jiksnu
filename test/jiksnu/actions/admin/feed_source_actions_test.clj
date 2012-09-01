(ns jiksnu.actions.admin.feed-source-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.admin.feed-source-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]])
  (:require [jiksnu.model.feed-source :as model.feed-source]))

(test-environment-fixture

 (fact "#'index"
   (fact "when there are no sources"
     (fact "should return an empty sequence"
       (:items (index)) => empty?))

   (fact "when there are many sources"
     (fact "should return a limited ammount"
       (dotimes [i 25]
         (model.feed-source/create (factory :feed-source)))

       ;; TODO: hardcoded configurable value
       (count (:items (index))) => 20)))

 )
