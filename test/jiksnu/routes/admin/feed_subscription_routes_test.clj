(ns jiksnu.routes.admin.feed-subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.routes-helper :only [get-auth-cookie response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock]))

(test-environment-fixture
 (fact "index"
   (let [password (fseq :password)
         user (model.user/create (factory :local-user {:admin true}))]
     (actions.auth/add-password user password)

     (let [cookie-str (get-auth-cookie (:username user) password)]
       (-> (mock/request :get "/admin/feed-subscriptions")
           (assoc-in [:headers "cookie"] cookie-str)
           response-for) =>
           (every-checker
            map?
            (comp status/success? :status)))))
 )
