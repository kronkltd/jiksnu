(ns jiksnu.routes.admin.user-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [as-admin response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [hiccup->doc test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact truthy => ]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.user :as model.user]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "admin show user"
   (fact "html"
     (let [user (mock/a-user-exists)]
       (mock/there-is-an-activity {:user user})
       (-> (mock/request :get (named-path "admin show user" {:id (:_id user)}))
           as-admin response-for) =>
           (every-checker
            map?
            (comp status/success? :status)
            (fn [response]
              (fact
                (let [body (:body response)]
                  body => (re-pattern (:username user))
                  (let [doc (hiccup->doc body)]
                    (-> doc
                        (enlive/select [:.user])) => truthy))))))))
 )
