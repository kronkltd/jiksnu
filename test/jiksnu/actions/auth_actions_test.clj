(ns jiksnu.actions.auth-actions-test
  (:require [cemerick.friend.credentials :as creds]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :refer [add-password login]]
            [jiksnu.mock :as mock]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(def password (fseq :password))
(def user (mock/a-user-exists {:password password}))

(fact "#'actions.auth/add-password"
  (let [mech (add-password user password)]
    mech => truthy

    (creds/bcrypt-verify password (:value mech))))

(fact "#'actions.auth/login"

  (fact "when the user doesn't exist"
    (login .username. .password.) => (throws))

  (fact "when given a valid password"
    (let [username (:username user)]
      (login username password) => truthy)))
