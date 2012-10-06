(ns jiksnu.filters.admin.key-filters-test
  (:use [ciste.core :only [with-serialization *serialization*]]
        [ciste.filters :only [filter-action]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [anything fact future-fact => every-checker]])
  (:require [jiksnu.actions.admin.key-actions :as actions.key]))

(test-environment-fixture

 (fact "filter-action #'actions.key/index"
   (let [action #'actions.key/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (let [request {:action action :serialization *serialization*}]
           (filter-action action request) =>
           (every-checker
            map?

            ))))))

 )
