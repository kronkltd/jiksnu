(ns jiksnu.modules.web.routes.group-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.model.group :as model.group]
            jiksnu.modules.web.routes.group-routes
            [jiksnu.helpers.routes :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: group-api/collection :get"
  (let [url "/model/groups"
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status HttpStatus/SC_OK})))

(facts "route: group-api/collection :post"
  (let [params (factory :group)
        url "/model/groups"
        request (-> (req/request :post url)
                    (req/body (json/write-str params))
                    (req/content-type "application/json"))
        response (response-for request)]
    response => (contains {:status HttpStatus/SC_SEE_OTHER})
    (let [location (get-in response [:headers "Location"])]
      (if location
        (let [request2 (req/request :get location)
              response2 (response-for request2)]
          response2 => (contains {:status HttpStatus/SC_OK})
          (let [body (json/read-str (:body response2) :key-fn keyword)]
            body => (contains {:name (:name params)})))
        (do
          location =not=> nil?)))))

(facts "route: group-api/item :delete"
  (fact "when not authenticated"
    (let [group (mock/a-group-exists)
          url (str "/model/groups/" (:_id group))
          request (-> (req/request :delete url))
          response (response-for request)]
      response => (contains {:status HttpStatus/SC_UNAUTHORIZED})
      (model.group/fetch-by-id (:_id group)) => group))
  (fact "when authenticated as the owner"
    (let [user (mock/a-user-exists)
          group (mock/a-group-exists {:user user})
          url (str "/model/groups/" (:_id group))
          request (-> (req/request :delete url)
                      (as-user user))
          response (response-for request)]
      response => (contains {:status HttpStatus/SC_NO_CONTENT})
      (model.group/fetch-by-id (:_id group)) => nil)))

(facts "route: group-api/item :get"
  (let [group (mock/a-group-exists)
        url (str "/model/groups/" (:_id group))
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status HttpStatus/SC_OK})))

(facts "route: group-api/members :get"
  (let [user (mock/a-user-exists)
        group (mock/a-group-exists {:members [user]})
        gm (mock/a-group-membership-exists {:user user
                                            :group group})
        path (str "/model/groups/" (:_id group) "/members")
        request (req/request :get path)
        response (response-for request)]

    response => (contains {:status HttpStatus/SC_OK})

    (some-> response :body (json/read-str :key-fn keyword)) =>
    (contains {:totalItems 1})))
