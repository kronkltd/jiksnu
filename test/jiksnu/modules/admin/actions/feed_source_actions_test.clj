(ns jiksnu.modules.admin.actions.feed-source-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.modules.admin.actions.feed-source-actions :only [index]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.mock :as mock]))

(test-environment-fixture

 (context #'index
   (context "when there are no sources"
     (:items (index)) => empty?)

   (context "when there are many sources"
     (dotimes [i 25]
       (mock/a-feed-source-exists))

     ;; TODO: hardcoded configurable value
     (count (:items (index))) => 20))

 )
