(ns jiksnu.modules.web.routes.group-routes-test
  (:require [ciste.model :as cm]
            [ciste.sections.default :refer [uri]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.group :as model.group]
            jiksnu.modules.web.routes.group-routes
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [puget.printer :as puget]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "route: group-api/collection :get"
  (let [url (str "/model/groups")
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status 200})))

(facts "route: group-api/collection :post"
  (let [params (factory :group)
        url (str "/model/groups")
        request (-> (req/request :post url)
                    (req/body (json/json-str params))
                    (req/content-type "application/json"))
        response (response-for request)
        location (get-in response [:headers "Location"])]
    response => (contains {:status 303})
    (let [request2 (req/request :get location)
          response2 (response-for request2)
          body (json/read-str (:body response2))]
      response2 => (contains {:status 200})
      body => (contains {"name" (:name params)}))))

(future-facts "route: group-api/item :delete"
  (fact "when not authenticated"
    (let [group (mock/a-group-exists)
          url (str "/model/groups/" (:_id group))
          request (-> (req/request :delete url))
          response (response-for request)]
      response => (contains {:status 401})
      (model.group/fetch-by-id (:_id group)) => group))
  (fact "when authenticated as the owner"
    (let [user (mock/a-user-exists)
          group (mock/a-group-exists {:user user})
          url (str "/model/groups/" (:_id group))
          request (-> (req/request :delete url)
                      (as-user user))
          response (response-for request)]
      (util/inspect group)
      response => (contains {:status 401})
      (model.group/fetch-by-id (:_id group)) => nil)))

(facts "route: group-api/item :get"
  (let [group (mock/a-group-exists)
        url (str "/model/groups/" (:_id group))
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status 200})))
