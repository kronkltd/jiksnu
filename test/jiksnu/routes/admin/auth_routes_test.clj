(ns jiksnu.routes.admin.auth-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "auth admin index"

   (fact "When not authenticated"
     (-> (mock/request :get "/admin/auth")
         response-for) =>
         (every-checker
          map?
          (comp status/redirect? :status)))

   (future-fact "When authenticated as an admin"
     (let [user (actions.user/create (factory :user {:admin true}))]
       (with-user user
         (-> (mock/request :get "/admin/auth")
            response-for))) =>
           (every-checker
            map?
            (comp status/success? :status))))
 
 )

