(ns jiksnu.modules.web.filters.auth-filters-test
  (:use [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (context "filter-action #'login :http"
   (with-serialization :http
     (let [request {:params {:username .username.
                             :password .password.}}]
       (filter-action #'actions.auth/login request))) => .response.
       (provided
         (actions.auth/login .user. .password.) => .response. :times 1
         (model.user/get-user .username.) => .user.))

 )
