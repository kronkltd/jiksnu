(ns jiksnu.model.group-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.group :only [delete create fetch-all]]
        [midje.sweet :only [fact => every-checker future-fact]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]))

(test-environment-fixture

 (fact "#'delete"
   (fact "when the record exists"
     (let [group (actions.group/create (factory :group))]
       (delete group) =>
       (every-checker
        #(= group %)))))

 (fact "#'fetch-all"
   (fact "when there are no groups"
     (model/drop-all!)
     (fetch-all) =>
     (every-checker
      empty?))

   (fact "when there are less than a page"
     (model/drop-all!)
     (dotimes [n 19]
       (actions.group/create (factory :group)))
     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 19)))

   (fact "when there is more than a page"
     (model/drop-all!)
     (dotimes [n 21]
       (actions.group/create (factory :group)))
     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 20))))
 
 )
