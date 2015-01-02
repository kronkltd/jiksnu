(ns jiksnu.modules.web.routes.activity-routes-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [full-uri]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [jiksnu.test-helper :only [check test-environment-fixture]]
        [midje.sweet :only [contains => fact future-fact]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.activity-views
            [ring.mock.request :as req])
  (:import jiksnu.model.User))


(test-environment-fixture

 (fact "update"
   (fact "when the user is authenticated"
     (let [author (mock/a-user-exists)
           content (fseq :content)
           data (json/json-str
                 {:content content})]
       data => string?)))

 (fact "show-http-route"
   (fact "when the user is not authenticated"
     (fact "and the activity does not exist"
       (let [author (mock/a-user-exists)
             activity (factory :activity)]
         (->> (str "/notice/" (:_id activity))
              (req/request :get)
              response-for) =>
              (contains {:status 404})))

     (fact "and there are activities"
       (let [activity (mock/there-is-an-activity)]
         (->> (str "/notice/" (:_id activity))
              (req/request :get)
              response-for) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               (:body response) => string?))))

   (fact "when the user is authenticated"
     (fact "when a private activity exists"
       (let [activity (mock/there-is-an-activity {:modifier "private"})]
         (-> (req/request :get (str "/notice/" (:_id activity)))
             as-user response-for) =>
             (check [response]
               response => map?
               (:status response) => status/redirect?)))))

 (future-fact "oembed"
   (fact "when the format is json"
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity)]
       (-> (req/request :get (with-context [:http :html]
                                (str "/main/oembed?format=json&url=" (full-uri activity))))
           response-for) =>
           (check [response]
             response => map?
             (:status response) => status/redirect?
             (:body response) => string?)))

   (fact "when the format is xml"
     (let [activity (mock/there-is-an-activity)]
       (-> (req/request :get (with-context [:http :html]
                                (str "/main/oembed?format=xml&url=" (full-uri activity))))
           response-for) =>
           (check [response]
             response => map?
             (:status response) => status/success?
             (:body response) => string?)))
   )
 )
