(ns jiksnu.modules.admin.routes.auth-routes-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.session :as session]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.admin"])

(future-fact "auth admin index"

  (fact "When not authenticated"
    (let [response (-> (req/request :get "/admin/auth")
                       response-for)]
      response => map?
      (:status response) => HttpStatus/SC_SEE_OTHER))

  (future-fact "When authenticated as an admin"
    (let [user (actions.user/create (factory :user {:admin true}))]
      (session/with-user user
        (let [response (-> (req/request :get "/admin/auth")
                           response-for)]
          response => map?
          (:status response) => HttpStatus/SC_OK)))))
