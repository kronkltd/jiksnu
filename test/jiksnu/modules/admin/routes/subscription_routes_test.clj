(ns jiksnu.modules.admin.routes.subscription-routes-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-admin response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.admin"])

;; TODO: This is a better test for the view
(future-fact "delete"
  (let [subscription (mock/a-subscription-exists)]
    (let [url (str "/admin/subscriptions/" (:_id subscription) "/delete")
          response (-> (req/request :post url)
                       as-admin
                       response-for)]

      (fact "returns a map"
        response => map?)

      (fact "returns a successful response"
        (:status response) => HttpStatus/SC_SEE_OTHER))))
