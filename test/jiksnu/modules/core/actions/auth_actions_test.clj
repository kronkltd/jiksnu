(ns jiksnu.modules.core.actions.auth-actions-test
  (:require [cemerick.friend.credentials :as creds]
            [clj-factory.core :refer [fseq]]
            [jiksnu.modules.core.actions.auth-actions :refer [add-password login]]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.auth/add-password"
  (let [password (fseq :password)
        user (mock/a-user-exists {:password password})
        mech (add-password user password)]
    mech => truthy

    (creds/bcrypt-verify password (:value mech))))

(future-fact "#'actions.auth/login"
  (let [password (fseq :password)
        user (mock/a-user-exists {:password password})]
    (fact "when the user doesn't exist"
      (login .username. .password.) => (throws))

    (fact "when given a valid password"
      (let [username (:username user)]
        (login username password) => truthy))))
