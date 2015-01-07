(ns jiksnu.actions.auth-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :refer [login]]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'login
  (let [password (fseq :password)
        user (mock/a-user-exists {:password password})]
    (login user password) => truthy))


