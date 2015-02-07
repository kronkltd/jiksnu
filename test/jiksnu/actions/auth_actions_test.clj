(ns jiksnu.actions.auth-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :refer [login]]
            #_[jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.auth/login" :focus
  (fact "when the user doesn't exist"
    (login .username. .password.) =not=> (throws)
)
  (future-fact "when given a valid password"
    (let [password (fseq :password)
          ;; user (mock/a-user-exists {:password password})
          ;; username (:username user)
          ]
      (login .username. password) => truthy)))


