(ns jiksnu.modules.web.routes.activity-routes-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.routes.activity-routes
            jiksnu.modules.web.views.activity-views
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> contains fact future-fact]]
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
