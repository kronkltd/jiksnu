(ns jiksnu.actions.auth-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :refer [login]]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=> truthy]]))

(test-environment-fixture

 (context #'login
   (let [password (fseq :password)
         user (mock/a-user-exists {:password password})]
     (login user password) => truthy))

 )
