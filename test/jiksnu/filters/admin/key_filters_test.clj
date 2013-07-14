(ns jiksnu.filters.admin.key-filters-test
  (:use [ciste.core :only [with-serialization *serialization*]]
        [ciste.filters :only [filter-action]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [anything =>]])
  (:require [jiksnu.actions.admin.key-actions :as actions.key]))

(test-environment-fixture

 (context "filter-action #'actions.key/index"
   (let [action #'actions.key/index]
     (context "when the serialization is :http"
       (with-serialization :http
         (let [request {:action action}]
           (filter-action action request) =>
           (check [response]
             response => map?))))))

 )
