(ns jiksnu.modules.web.routes.user-routes-test
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.modules.web.routes :as routes]
            jiksnu.modules.web.views.user-views
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "index page"
  (routes/set-site)
  (let [url "/main/users"]
    (response-for (req/request :get url)) =>
    (contains {:status status/success?
               :body string?})))

