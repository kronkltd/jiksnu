(ns jiksnu.modules.admin.routes.feed-subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.routes-helper :only [as-admin get-auth-cookie response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as req]))

(test-environment-fixture
 (context "index"
   (let [feed-subscription (mock/a-feed-subscription-exists)]
     (-> (req/request :get "/admin/feed-subscriptions")
         as-admin response-for) =>
         (check [response]
           (let [body (:body response)]
             response => map?
             (:status response) => status/success?
             ;; body => #"feed-subscriptions"
             body => (re-pattern (str (:_id feed-subscription)))))))
 )
