(ns jiksnu.actions.auth-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.auth-actions :only [login]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=> truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]))

(test-environment-fixture

 (context #'login
   (let [password (fseq :password)
         user (mock/a-user-exists {:password password})]
     (login user password) => truthy))

 )
