(ns jiksnu.actions.admin.feed-subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.admin.feed-subscription-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact =>]])
  (:require [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(test-environment-fixture

 (fact "#'index"
   (fact "when there are no sources"
     (db/drop-all!)

     (index) =>
     (every-checker
      map?
      (comp empty? :items)
      #(zero? (:totalRecords %))

      ))

   (fact "when there are more than the page size sources"
     (db/drop-all!)

     (dotimes [n 25]
       (mock/a-feed-subscription-exists))

     (index) =>
     (every-checker
      #(fact (count (:items %)) => 20))))

 )
