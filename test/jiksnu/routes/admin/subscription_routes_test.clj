(ns jiksnu.routes.admin.subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-admin get-auth-cookie response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [hiccup.core :as h]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "index page"
   (let [subscription (existance/a-subscription-exists)]
     (-> (mock/request :get "/admin/subscriptions")
         as-admin response-for) =>
         (every-checker
          (comp status/success? :status)
          (fn [req]
            (fact
              (let [body (h/html (:body req))]
                body => #"subscription"))))))
 
 (fact "delete"
   (let [subscription (model.subscription/create (factory :subscription))]
     (-> (mock/request :post (str "/admin/subscriptions/" (:_id subscription) "/delete"))
         as-admin response-for) =>
         (every-checker
          map?
          (comp status/redirect? :status))))
 
 )
