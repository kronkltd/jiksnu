(ns jiksnu.routes.admin.subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [get-auth-cookie response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [hiccup.core :as h]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "index page"
   (let [password (fseq :password)
         user (model.user/create (factory :local-user {:admin true}))]
     (actions.auth/add-password user password)
     (let [cookie-str (get-auth-cookie (:username user) password)]
       (-> (mock/request :get "/admin/subscriptions")
           (assoc-in [:headers "cookie"] cookie-str)
           response-for)) =>
         (every-checker
          (contains {:status 200})
          (fn [req]
            (fact
              (let [body (h/html (:body req))]
                body => #"subscription"))))))
 )
