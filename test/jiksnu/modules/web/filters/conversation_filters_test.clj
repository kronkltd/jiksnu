(ns jiksnu.modules.web.filters.conversation-filters-test
  (:use [clj-factory.core :only [factory fseq]]
        [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "filter-action #'actions.conversation/create"
   (let [action #'actions.conversation/create]
     (context "when the serialization is :http"
       (with-serialization :http
         (let [request {:params .params.}]
           (filter-action action request) => .response.
           (provided
             (actions.conversation/create .params.) => .response.))))))

 (context "filter-action #'actions.conversation/delete"
   (let [action #'actions.conversation/delete]
     (context "when the serialization is :http"
       (with-serialization :http
         (let [request {:params {:id .id.}}]
           (filter-action action request) => .response.
           (provided
             (model.conversation/fetch-by-id .id.) => .conversation.
             (actions.conversation/delete .conversation.) => .response.))))))

 (context "filter-action #'actions.conversation/index"
   (let [action #'actions.conversation/index]
     (context "when the serialization is :http"
       (with-serialization :http
         (let [request {}]
           (filter-action action request) =>
           (check [response]
             response => map?
             (:items response) => empty?
             (:totalRecords response) => zero?))))))
 )
