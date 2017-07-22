(ns jiksnu.modules.web.routes.album-routes-test
  (:require [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.helpers.routes :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: albums-api/collection :get"
  (fact "when there is an album"
    (let [url "/model/albums"]
      (let [album  (mock/an-album-exists)
            response (response-for (req/request :get url))]
        response =>
        (contains {:status HttpStatus/SC_OK
                   :body   string?})
        (let [body (some-> response :body (json/read-str :key-fn keyword))]
          body => (contains {:totalItems 1
                             :items (contains (str (:_id album)))}))))))

(fact "route: albums-api/item :delete"
  (fact "when not authenticated"
    (let [album (mock/an-album-exists)
          path (str "/model/albums/" (:_id album))
          response (response-for (req/request :delete path))]
      response => (contains {:status HttpStatus/SC_UNAUTHORIZED}))))
