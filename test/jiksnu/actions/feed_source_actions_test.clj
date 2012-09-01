(ns jiksnu.actions.feed-source-actions-test
  (:use [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.actions.feed-source-actions
        [midje.sweet :only [fact future-fact => every-checker truthy]]))

(test-environment-fixture

 (fact "#'add-watcher"
   (add-watcher .source. .user.) => truthy
   )
 )
