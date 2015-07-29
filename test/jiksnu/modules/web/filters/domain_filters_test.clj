(ns jiksnu.modules.web.filters.domain-filters-test
  (:require [ciste.core :refer [with-serialization with-format]]
            [ciste.filters :refer [filter-action]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.domain :as model.domain]
            jiksnu.modules.web.filters.domain-filters
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "filter-action #'actions.domain/show"
  (let [action #'actions.domain/show]
    (fact "when the serialization is :http"
      (with-serialization :http
        (let [request {:params {:id .id.}}]
          (filter-action action request) => .response.
          (provided
            (actions.domain/show .domain.) => .response.
            (model.domain/fetch-by-id .id.) => .domain.))))))
