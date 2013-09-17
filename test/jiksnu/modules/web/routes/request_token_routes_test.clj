(ns jiksnu.modules.web.routes.request-token-routes-test
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=>]]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "authorize"
   (context "when given a valid request token"
     (let [request-token (mock/a-request-token-exists)
           url (format "/oauth/authorize?oauth_token=%" (:_id request-token))]
       (-> (req/request :get url)
           response-for) =>
           (check [response]
             (:status response) => status/success?
             )
       )
     )
   )
 )
