(ns jiksnu.modules.web.routes.stream-routes-test
  (:use [ciste.formats :only [format-as]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.stream-views
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "public-timeline-http-route"
   (context "when there are no activities"
     (db/drop-all!)

     (-> (req/request :get "/")
         response-for) =>
         (check [response]
           response => map?
           (:status response) => status/success?))

   (context "when there are activities"
     (let [user (mock/a-user-exists)]
       (dotimes [n 10]
         (mock/there-is-an-activity {:user user}))

       (context "when the user is not authenticated"
         (-> (req/request :get "/")
             response-for) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               (:body response) => string?))

       (context "when the the request is for n3"
         (-> (req/request :get "/api/statuses/public_timeline.n3")
             response-for) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               ;; TODO: parse and check model
               (let [body (:body response)]
                 body => string?)))

       (context "when the user is authenticated"
         (-> (req/request :get "/")
             as-user
             response-for) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               (:body response) => string?))
       ))
   )

 (context "user timeline"

   (context "html"
     (let [user (mock/a-user-exists)]
       (dotimes [n 10]
         (mock/there-is-an-activity {:user user}))

       (-> (req/request :get (format "/%s" (:username user)))
           as-user response-for)) =>
           (check [response]
             response => map?
             (:status response) => status/success?
             (:body response) => string?))

   (context "n3"
     (let [user (mock/a-user-exists)]
       (dotimes [n 10]
         (mock/there-is-an-activity {:user user}))

       (-> (req/request :get (format "/api/statuses/user_timeline/%s.n3" (:_id user)))
           (as-user user) response-for)) =>
           (check [response]
             response => map?
             (:status response) => status/success?
             (:body response) => string?))
   )
 )
