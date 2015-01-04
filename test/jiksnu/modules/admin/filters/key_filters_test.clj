(ns jiksnu.modules.admin.filters.key-filters-test
  (:use [ciste.core :only [with-serialization *serialization*]]
        [ciste.filters :only [filter-action]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> fact]])
  (:require [jiksnu.modules.admin.actions.key-actions :as actions.key]))

(test-environment-fixture

 (fact "filter-action #'actions.key/index"
   (let [action #'actions.key/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (let [request {:action action}]
           (let [response (filter-action action request)]
             response => map?))))))

 )
