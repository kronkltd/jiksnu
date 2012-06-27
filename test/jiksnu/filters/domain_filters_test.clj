(ns jiksnu.filters.domain-filters-test
    (:use [clj-factory.core :only [factory]]
        [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact =>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.domain :as model.domain]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "filter-action #'actions.domain/show"
   (let [action #'actions.domain/show]
     (fact "when the serialization is :http"
       (with-serialization :http
         (let [request {:params {:id .id.}}]
           (filter-action action request)) => .response.
           (provided
             (actions.domain/show .domain.) => .response.
             (model.domain/fetch-by-id .id.) => .domain.)))))

 )
