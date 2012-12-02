(ns jiksnu.actions.admin.feed-subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.admin.feed-subscription-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact =>]])
  (:require [jiksnu.existance-helpers :as existance]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(test-environment-fixture

 (fact "#'index"
   (fact "when there are no sources"
     (model/drop-all!)

     (index) =>
     (every-checker
      map?
      (comp empty? :items)
      #(zero? (:total-records %))

      ))

   (fact "when there are more than the page size sources"
     (model/drop-all!)

     (dotimes [n 25]
       (existance/a-feed-subscription-exists))

     (index) =>
     (every-checker
      #(fact (count (:items %)) => 20))))

 )
