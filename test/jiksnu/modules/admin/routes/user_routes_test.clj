(ns jiksnu.modules.admin.routes.user-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.core.incubator :refer [-?> -?>>]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.user :as model.user]
            [jiksnu.routes.helpers :refer [named-path]]
            [jiksnu.routes-helper :refer [as-admin response-for]]
            [jiksnu.test-helper :refer [check context future-context
                                        hiccup->doc test-environment-fixture]]
            [midje.sweet :refer [truthy =>]]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "admin show user"
   (context "html"
     (let [user (mock/a-user-exists)]
       (mock/there-is-an-activity {:user user})
       (let [path (named-path "admin show user" {:id (:_id user)})]
         (-> (req/request :get path)
             as-admin response-for)) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               (let [body (:body response)]
                 body => (re-pattern (:username user))
                 #_(let [doc (hiccup->doc body)]
                     (enlive/select doc [:.user]) => truthy))))))
 )
