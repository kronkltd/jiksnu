(ns jiksnu.modules.web.filters.conversation-filters-test
  (:use [clj-factory.core :only [factory fseq]]
        [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> fact]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [ring.mock.request :as req]))

(test-environment-fixture

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
             (:totalRecords response) => zero?))))))
 )
