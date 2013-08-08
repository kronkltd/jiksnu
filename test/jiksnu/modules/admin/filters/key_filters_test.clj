(ns jiksnu.modules.admin.filters.key-filters-test
  (:use [ciste.core :only [with-serialization *serialization*]]
        [ciste.filters :only [filter-action]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.modules.admin.actions.key-actions :as actions.key]))

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
