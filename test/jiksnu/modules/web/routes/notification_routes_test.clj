(ns jiksnu.modules.web.routes.notification-routes-test
  (:require [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.helpers.routes :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: notification-api/collection :get"
  (let [notification (mock/a-notification-exists)
        path "/model/notifications"
        request (req/request :get path)
        response (response-for request)]
    response => (contains {:status HttpStatus/SC_OK})
    (let [body (json/read-str (:body response) :key-fn keyword)]
      body => (contains {:totalItems 1
                         :items (contains (str (:_id notification)))}))))
