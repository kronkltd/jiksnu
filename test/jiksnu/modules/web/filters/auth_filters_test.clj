(ns jiksnu.modules.web.filters.auth-filters-test
  (:require [ciste.core :refer [with-serialization]]
            [ciste.filters :refer [filter-action]]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.filters.auth-filters
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "filter-action #'login :http"
  (with-serialization :http
    (let [request {:params {:username .username.
                            :password .password.}}]
      (filter-action #'actions.auth/login request))) => .response.
      (provided
        (actions.auth/login .user. .password.) => .response. :times 1
        (model.user/get-user .username.) => .user.))


