(ns jiksnu.modules.web.routes.picture-routes-test
  (:require [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: pictures-api/index :get"
  (fact "when there is a picture"
    (let [url "/model/pictures"]
      (let [picture  (mock/a-picture-exists)
            response (response-for (req/request :get url))]
        response =>
        (contains {:status HttpStatus/SC_OK
                   :body   string?})
        (let [body (some-> response :body (json/read-str :key-fn keyword))]
          body => (contains {:totalItems 1}))))))

(future-fact "route: pictures-api/item :delete"
  (fact "when not authenticated"
    (let [picture (mock/a-picture-exists)
          path (str "/model/pictures/" (:_id picture))
          response (response-for (req/request :delete path))]
      response => (contains {:status HttpStatus/SC_UNAUTHORIZED}))))
