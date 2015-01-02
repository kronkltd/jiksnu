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
