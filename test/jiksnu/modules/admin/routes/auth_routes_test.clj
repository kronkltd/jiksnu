(ns jiksnu.modules.admin.routes.auth-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.session :as session]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "auth admin index"

  (fact "When not authenticated"
    (let [response (-> (req/request :get "/admin/auth")
                       response-for)]
      response => map?
      (:status response) => status/redirect?))

  (future-fact "When authenticated as an admin"
    (let [user (actions.user/create (factory :user {:admin true}))]
      (session/with-user user
        (let [response (-> (req/request :get "/admin/auth")
                           response-for)]
          response => map?
          (:status response) => status/success?))))

  )


