(ns jiksnu.modules.web.routes.group-membership-routes-test
  (:require [ciste.sections.default :refer [uri]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.routes.group-membership-routes
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: group-memberships-api/collection :get"
  (let [group-membership (mock/a-group-membership-exists)
        path "/model/group-memberships"
        request (req/request :get path)
        response (response-for request)]
    response => (contains {:status HttpStatus/SC_OK})
    (let [body (json/read-str (:body response) :key-fn keyword)]
      body => (contains {:totalItems 1}))))
