(ns jiksnu.routes.stream-routes-test
  (:use [ciste.formats :only [format-as]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [midje.sweet :only [fact future-fact => every-checker]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "public-timeline-http-route"
   (fact "when there are no activities"
     (model/drop-all!)

     (-> (mock/request :get "/")
         response-for) =>
         (every-checker
          map?
          (comp status/success? :status)
          ;; TODO: check count == 0
          ))

   (fact "when there are activities"
     (let [user (existance/a-user-exists)]
       (dotimes [n 10]
         (existance/there-is-an-activity {:user user}))

       (fact "when the user is not authenticated"
         (-> (mock/request :get "/")
             response-for) =>
             (every-checker
              map?
              (comp status/success? :status)))

       (fact "when the the request is for n3"
         (-> (mock/request :get "/api/statuses/public_timeline.n3")
             response-for) =>
             (every-checker
              map?
              (comp status/success? :status)
              (comp string? :body)
              ;; TODO: parse and check model
              ))

       (fact "when the user is authenticated"
         (-> (mock/request :get "/")
             as-user
             response-for) =>
             (every-checker
              map?
              (comp status/success? :status))))))


 (fact "user timeline"

   (fact "html"

     (let [user (existance/a-user-exists)]
       (dotimes [n 10]
         (existance/there-is-an-activity {:user user}))

       (-> (mock/request :get (format "/%s" (:username user)))
           as-user response-for)) =>
           (every-checker
            map?
            (comp status/success? :status)))

   (fact "n3"
     (let [user (existance/a-user-exists)]
       (dotimes [n 10]
         (existance/there-is-an-activity {:user user}))

       (-> (mock/request :get (format "/api/statuses/user_timeline/%s.n3" (:_id user)))
           (as-user user) response-for)) =>
           (every-checker
            map?
            (comp status/success? :status)
            (comp string? :body))))
 )
