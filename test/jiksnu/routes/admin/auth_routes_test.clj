(ns jiksnu.routes.admin.auth-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "auth admin index"

   (context "When not authenticated"
     (-> (req/request :get "/admin/auth")
         response-for) =>
         (check [response]
                   response => map?
                   (:status response) => status/redirect?))

   (future-context "When authenticated as an admin"
     (let [user (actions.user/create (factory :user {:admin true}))]
       (with-user user
         (-> (req/request :get "/admin/auth")
             response-for))) =>
             (check [response]
               response => map?
               (:status response) => status/success?))

   )
 )

