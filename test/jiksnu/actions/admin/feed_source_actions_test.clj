(ns jiksnu.actions.admin.feed-source-actions-test
  (:use (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.actions.admin.feed-source-actions
        midje.swee))

(test-environment-fixture

 (fact "#'index"
   (future-fact "when there are no sources"
     (fact "should return an empty sequence"))

   (future-fact "when there are many sources"
     (fact "should return a limited ammount"
       (let [response (index)]
         ;; TODO: hardcoded configurable value
         (count response) => 20))))

 )
