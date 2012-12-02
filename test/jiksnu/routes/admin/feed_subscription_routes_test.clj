(ns jiksnu.routes.admin.feed-subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.routes-helper :only [as-admin get-auth-cookie response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock]))

(test-environment-fixture
 (fact "index"
   (let [feed-subscription (existance/a-feed-subscription-exists)]
     (-> (mock/request :get "/admin/feed-subscriptions")
         as-admin response-for) =>
         (every-checker
          map?
          (comp status/success? :status)
          (fn [result]
            (let [body (:body result)]
              (fact
                ;; body => #"feed-subscriptions"
                body => (re-pattern (str (:_id feed-subscription)))))))))
 )
