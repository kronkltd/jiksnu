(ns jiksnu.routes.activity-routes-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [full-uri]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [contains every-checker fact future-fact =>]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [ring.mock.request :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.User))


(test-environment-fixture

 (fact "update"
   (fact "when the user is authenticated"
     (let [author (feature/a-user-exists)
           content (fseq :content)
           data (json/json-str
                 {:content content})]
       data => string?)))
 
 (fact "show-http-route"
   (fact "when the user is not authenticated"
     (fact "and the activity does not exist"
       (let [author (feature/a-user-exists)
             activity (factory :activity)]
         (->> (str "/notice/" (:_id activity))
              (mock/request :get)
              response-for) =>
              (contains {:status 404})))

     (fact "and there are activities"
       (let [activity (feature/there-is-an-activity)]
         (->> (str "/notice/" (:_id activity))
              (mock/request :get)
              response-for) =>
              (every-checker
               (comp status/success? :status)
               (fn [response]
                 (fact
                   (:body response) => (re-pattern (str (:_id activity)))))))))
   (fact "when the user is authenticated"
     (fact "when a private activity exists"
       (let [activity (feature/there-is-an-activity {:modifier "private"})]
         (-> (mock/request :get (str "/notice/" (:_id activity)))
             as-user response-for) =>
             (every-checker
              map?
              (comp status/redirect? :status))))))
 
 (future-fact "oembed"
   (fact "when the format is json"
     (let [user (feature/a-user-exists)
           activity (feature/there-is-an-activity)]
       (-> (mock/request :get (with-context [:http :html]
                                (str "/main/oembed?format=json&url=" (full-uri activity))))
           response-for) =>
           (every-checker
            map?
            (fn [response]
              (fact
                (:status response) => status/success?)))))
   (fact "when the format is xml"
     (let [activity (feature/there-is-an-activity)]
       (-> (mock/request :get (with-context [:http :html]
                                (str "/main/oembed?format=xml&url=" (full-uri activity))))
           response-for) =>
           (every-checker
            map?
            (comp status/success? :status)))))
 )
