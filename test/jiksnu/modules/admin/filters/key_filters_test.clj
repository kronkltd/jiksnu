(ns jiksnu.modules.admin.filters.key-filters-test
  (:require [ciste.core :refer [with-serialization *serialization*]]
            [ciste.filters :refer [filter-action]]
            [jiksnu.modules.admin.actions.key-actions :as actions.key]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "filter-action #'actions.key/index"
  (let [action #'actions.key/index]
    (fact "when the serialization is :http"
      (with-serialization :http
        (let [request {:action action}]
          (filter-action action request) => map?)))))


