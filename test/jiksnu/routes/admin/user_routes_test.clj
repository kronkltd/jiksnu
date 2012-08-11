(ns jiksnu.routes.admin.user-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojure.core.incubator :only [-?> -?>>]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes-helper :only [as-admin get-auth-cookie response-for]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [hiccup->doc test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact truthy => ]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.user :as model.user]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as mock]))

(test-environment-fixture
 (fact "admin show user"
   (fact "html"
     (let [user (model.user/create (factory :local-user))]
       (with-user user
         (actions.activity/post (factory :activity))
         )
       (-> (mock/request :get (named-path "admin show user" {:id (:_id user)}))
           as-admin response-for
           ) =>
             (every-checker
              map?
              (comp status/success? :status)
              (fn [response]
                (fact
                  (let [body (:body response)]
                    body => (re-pattern (:username user))
                    (let [doc (hiccup->doc (log/spy body))]
                     (-> doc
                         (enlive/select [:.user])
                         ) => truthy
                     )))
                )
              )

       )

     )
   ))
