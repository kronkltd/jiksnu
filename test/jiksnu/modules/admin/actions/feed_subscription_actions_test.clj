(ns jiksnu.modules.admin.actions.feed-subscription-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.modules.admin.actions.feed-subscription-actions
             :as actions.feed-subscription]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact #'actions.feed-subscription/index
   (fact "when there are no sources"
     (db/drop-all!)

     (let [response (actions.feed-subscription/index)]
       response => map?
       (:items response) => empty?
       (:totalItems response) => zero?))

   (fact "when there are more than the page size sources"
     (db/drop-all!)

     (dotimes [n 25]
       (mock/a-feed-subscription-exists))

     (let [response (actions.feed-subscription/index)]
       (count (:items response)) => 20)))

 )
