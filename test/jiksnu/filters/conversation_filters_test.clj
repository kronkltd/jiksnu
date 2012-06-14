(ns jiksnu.filters.conversation-filters-test
  (:use [clj-factory.core :only [factory fseq]]
        [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [anything fact future-fact => every-checker truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            jiksnu.filters.stream-filters
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [ring.mock.request :as mock]))

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
           (filter-action action request) => .response.
           (provided
             (actions.conversation/index anything) => .response.))))))
 )
