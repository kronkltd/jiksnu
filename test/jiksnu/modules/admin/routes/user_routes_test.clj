(ns jiksnu.modules.admin.routes.user-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [as-admin response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [check context future-context
                                   hiccup->doc test-environment-fixture]]
        [midje.sweet :only [truthy =>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.user :as model.user]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "admin show user"
   (context "html"
     (let [user (log/spy :info (mock/a-user-exists))]
       (mock/there-is-an-activity {:user user})
       (-> (req/request :get (log/spy :info (named-path "admin show user" {:* (:_id user)})))
           as-admin response-for) =>
           (check [response]
             response => map?
             (:status response) => status/success?
             (let [body (:body response)]
               body => (re-pattern (:username user))
               #_(let [doc (hiccup->doc body)]
                 (enlive/select doc [:.user]) => truthy))))))
 )
