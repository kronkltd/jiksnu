(ns jiksnu.modules.web.routes.admin.subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-admin get-auth-cookie response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [hiccup.core :as h]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "index page"
   (let [subscription (mock/a-subscription-exists)]
     (-> (req/request :get "/admin/subscriptions")
         as-admin response-for) =>
         (check [response]
           response => map?
           (:status response) => status/success?
           (let [body (h/html (:body response))]
             body => #"subscription"))))

 (context "delete"
   (let [subscription (mock/a-subscription-exists)]
     (-> (req/request :post (str "/admin/subscriptions/" (:_id subscription) "/delete"))
         as-admin response-for) =>
         (check [response]
           response => map?
           (:status response) => status/redirect?)))

 )
