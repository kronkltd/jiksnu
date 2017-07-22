(ns jiksnu.modules.web.routes.service-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.db :as db]
            jiksnu.modules.web.routes.service-routes
            [jiksnu.test-helper :as th]
            [jiksnu.helpers.routes :refer [as-user response-for]]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: services-api/collection :get"
  (fact "when there is an service"
    (let [url "/model/services"]
      (let [album  (mock/a-service-exists)
            response (response-for (req/request :get url))]
        response =>
        (contains {:status HttpStatus/SC_OK
                   :body   string?})
        (let [body (some-> response :body (json/read-str :key-fn keyword))]
          body => (contains {:totalItems 1
                             :items (contains (str (:_id album)))}))))))

(fact "route: services-api/collection :post"
  (db/drop-all!)
  (let [params {:name (fseq :word)}
        actor (mock/a-user-exists)
        request (-> (req/request :post "/model/services")
                    (req/body (json/write-str params))
                    (req/content-type "application/json")
                    (as-user actor))
        response (response-for request)]

    response =>
    (contains {:status  HttpStatus/SC_SEE_OTHER
               :headers (contains {"Location" #"/model/services/[\d\w]+"})})

    (let [location (get-in response [:headers "Location"])]

      location => string?

      (let [request2 (req/request :get location)]
        (let [response2 (response-for request2)]
          response2 => (contains {:status HttpStatus/SC_OK})
          (some-> response2 :body json/read-str) =>
          (contains {"name" (:name params)}))))))

(facts "route: services-api/collection :delete"
  (fact "when not authenticated"
    (let [service (mock/a-service-exists)
          url (str "/model/services/" (:_id service))
          request (-> (req/request :delete url))]
      (response-for request) => (contains {:status HttpStatus/SC_UNAUTHORIZED})))
  (fact "when authenticated as a non-privileged user"
    (let [user (mock/a-user-exists)
          service (mock/a-service-exists {:user user})
          url (str "/model/services/" (:_id service))
          user2 (mock/a-user-exists)
          request (-> (req/request :delete url) (as-user user2))]
      (response-for request) => (contains {:status HttpStatus/SC_FORBIDDEN}))))

(facts "route: services-api/item :get"
  (let [item (mock/a-service-exists)
        url (str "/model/services/" (:_id item))
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status HttpStatus/SC_OK})))
