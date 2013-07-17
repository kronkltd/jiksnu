(ns jiksnu.actions.admin.feed-subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.admin.feed-subscription-actions :only [index]]
        [jiksnu.test-helper :only [check context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(test-environment-fixture

 (context #'index
   (context "when there are no sources"
     (db/drop-all!)

     (index) =>
     (check [response]
       response => map?
       (:items response) => empty?
       (:totalRecords response) => zero?))

   (context "when there are more than the page size sources"
     (db/drop-all!)

     (dotimes [n 25]
       (mock/a-feed-subscription-exists))

     (index) =>
     (check [response]
       (count (:items response)) => 20)))

 )
