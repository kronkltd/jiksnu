(ns jiksnu.filters.user-filters-test
  (:use [clj-factory.core :only [factory]]
        [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact =>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [ring.mock.request :as req]))


(test-environment-fixture

 (fact "filter-action #'index :http"
   (let [action #'actions.user/index]
     (with-serialization :http
       (let [request {}]
         (filter-action action request) =>
         (every-checker
          map?)))))

 )

