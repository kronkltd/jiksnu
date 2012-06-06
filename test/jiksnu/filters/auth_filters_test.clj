(ns jiksnu.filters.auth-filters-test
  (:use [ciste.filters :only [filter-action]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker]])
  (:require [jiksnu.actions.auth-actions :as actions.auth]))

(test-environment-fixture

 (fact "filter-action #'login :http"
   (filter-action #'actions.auth/login
                  {:serialization :http
                   :format :html
                   :params {:username .username.
                            :password .password.}}) => .response.
   (provided
     (actions.auth/login .username. .password.) => .response. :times 1))

 )
