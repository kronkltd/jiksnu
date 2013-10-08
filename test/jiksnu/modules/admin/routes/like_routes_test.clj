(ns jiksnu.modules.admin.routes.like-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.routes-helper :only [as-admin response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as req]))

(test-environment-fixture

 (future-context "delete"
   (let [like (model.like/create (factory :like))]
     (-> (req/request :post (str "/admin/likes/" (:_id like) "/delete"))
         as-admin response-for) =>
         (check [response]
           response => map?
           (:status response) => status/redirect?)))
 )
