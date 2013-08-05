(ns jiksnu.modules.web.routes.activity-routes-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [full-uri]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [contains =>]])
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

 (context "update"
   (context "when the user is authenticated"
     (let [author (mock/a-user-exists)
           content (fseq :content)
           data (json/json-str
                 {:content content})]
       data => string?)))

 (context "show-http-route"
   (context "when the user is not authenticated"
     (context "and the activity does not exist"
       (let [author (mock/a-user-exists)
             activity (factory :activity)]
         (->> (str "/notice/" (:_id activity))
              (req/request :get)
              response-for) =>
              (contains {:status 404})))

     (context "and there are activities"
       (let [activity (mock/there-is-an-activity)]
         (->> (str "/notice/" (:_id activity))
              (req/request :get)
              response-for) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               (:body response) => string?
               (:body response) => (re-pattern (str (:_id activity)))))))

   (context "when the user is authenticated"
     (context "when a private activity exists"
       (let [activity (mock/there-is-an-activity {:modifier "private"})]
         (-> (req/request :get (str "/notice/" (:_id activity)))
             as-user response-for) =>
             (check [response]
               response => map?
               (:status response) => status/redirect?)))))

 (future-context "oembed"
   (context "when the format is json"
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity)]
       (-> (req/request :get (with-context [:http :html]
                                (str "/main/oembed?format=json&url=" (full-uri activity))))
           response-for) =>
           (check [response]
             response => map?
             (:status response) => status/redirect?
             (:body response) => string?)))

   (context "when the format is xml"
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
