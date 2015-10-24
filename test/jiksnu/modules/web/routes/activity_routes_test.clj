(ns jiksnu.modules.web.routes.activity-routes-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [fseq]]
            [clojure.data.json :as json]
            [taoensso.timbre :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.activity-views
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import jiksnu.model.User))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "route: activity/update"
  (fact "when the user is authenticated"
    (let [author (mock/a-user-exists)
          content (fseq :content)
          data (json/json-str
                {:content content})]
      data => string?)))

(future-fact "oembed"
  (fact "when the format is json"
    (let [activity (mock/there-is-an-activity)
          url (str "/main/oembed?format=json&url=" (:url activity))]
      (response-for (req/request :get url)) =>
      (contains {:status status/redirect?
                 :body string?})))

  (fact "when the format is xml"
    (let [activity (mock/there-is-an-activity)
          url (str "/main/oembed?format=xml&url=" (:url activity))]
      (response-for (req/request :get url)) =>
      (contains {:status status/success?
                 :body string?})))
  )
