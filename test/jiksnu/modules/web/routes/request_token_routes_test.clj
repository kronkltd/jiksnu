(ns jiksnu.modules.web.routes.request-token-routes-test
  (:require [clojurewerkz.support.http.statuses :as status]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(future-fact "authorize"
  (fact "when authenticated"
    (let [actor (mock/a-user-exists)]

      (fact "when given a valid request token"
        (let [request-token (mock/a-request-token-exists)
              url (format "/oauth/authorize?oauth_token=%s" (:_id request-token))]
          (let [response (-> (req/request :get url)
                             (as-user actor)
                             response-for)]
            (:status response) => status/success?))))))
