(ns jiksnu.actions.feed-source-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.feed-source-actions :only [add-watcher]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker truthy]])
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (fact "#'add-watcher"
   (let [user (existance/a-user-exists)
         source (model.feed-source/create (factory :feed-source))]
     (add-watcher source user)) => truthy)
 )
