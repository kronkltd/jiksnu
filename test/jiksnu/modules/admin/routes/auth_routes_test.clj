(ns jiksnu.modules.admin.routes.auth-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check test-environment-fixture]]
        [midje.sweet :only [=> fact future-fact]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [ring.mock.request :as req]))

(test-environment-fixture

 (fact "auth admin index"

   (fact "When not authenticated"
     (-> (req/request :get "/admin/auth")
         response-for) =>
         (check [response]
                   response => map?
                   (:status response) => status/redirect?))

   (future-fact "When authenticated as an admin"
     (let [user (actions.user/create (factory :user {:admin true}))]
       (session/with-user user
         (-> (req/request :get "/admin/auth")
             response-for))) =>
             (check [response]
               response => map?
               (:status response) => status/success?))

   )
 )

