(ns jiksnu.modules.web.routes.like-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.model.like :as model.like]
            [jiksnu.routes-helper :refer [as-admin response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import org.apache.http.HttpStatus))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: likes-api/collection :get"
  (let [url "/model/likes"
        like (mock/a-like-exists)
        request (req/request :get url)]
    (let [response (response-for request)]
      response => (contains
                   {:status HttpStatus/SC_OK})
      (some-> response :body (json/read-str :key-fn keyword)) =>
      (contains {:items (contains (str (:_id like)))}))))

(fact "route: likes-api/item :delete"
  (let [like (mock/a-like-exists)
        url (format "/model/likes/%s" (:_id like))]
    (-> (req/request :delete url)
        as-admin response-for) =>
    (contains {:status HttpStatus/SC_NO_CONTENT})

    (model.like/fetch-by-id (:_id like)) => nil))
