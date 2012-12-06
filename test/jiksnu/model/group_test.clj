(ns jiksnu.model.group-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.group :only [create delete fetch-all fetch-by-id]]
        [midje.sweet :only [fact => every-checker future-fact]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.db :as db]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.util :as util]))

(test-environment-fixture

 (fact "#'fetch-by-id"
   (fact "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the item exists"
     (let [item (existance/a-group-exists)]
      (fetch-by-id (:_id item)) => item)))

 (fact "#'delete"
   (fact "when the item exists"
     (let [item (existance/a-group-exists)]
       (delete item) =>  item)))

 (fact "#'fetch-all"
   (fact "when there are no items"
     (db/drop-all!)
     (fetch-all) =>
     (every-checker
      empty?))

   (fact "when there are less than a page"
     (db/drop-all!)
     (dotimes [n 19]
       (actions.group/create (factory :group)))
     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 19)))

   (fact "when there is more than a page"
     (db/drop-all!)
     (dotimes [n 21]
       (actions.group/create (factory :group)))
     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 20))))

 )
