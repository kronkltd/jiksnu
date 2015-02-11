(ns jiksnu.modules.web.filters.conversation-filters-test
  (:require [ciste.core :refer [with-serialization]]
            [ciste.filters :refer [filter-action]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "filter-action #'actions.conversation/create"
  (let [action #'actions.conversation/create]
    (fact "when the serialization is :http"
      (with-serialization :http
        (let [request {:params .params.}]
          (filter-action action request) => .response.
          (provided
            (actions.conversation/create .params.) => .response.))))))

(fact "filter-action #'actions.conversation/delete"
  (let [action #'actions.conversation/delete]
    (fact "when the serialization is :http"
      (with-serialization :http
        (let [request {:params {:id .id.}}]
          (filter-action action request) => .response.
          (provided
            (model.conversation/fetch-by-id .id.) => .conversation.
            (actions.conversation/delete .conversation.) => .response.))))))

(fact "filter-action #'actions.conversation/index"
  (let [action #'actions.conversation/index]
    (fact "when the serialization is :http"
      (with-serialization :http
        (let [request {}]
          (let [response (filter-action action request)]
            response => map?
            (:items response) => empty?
            (:totalItems response) => zero?))))))

