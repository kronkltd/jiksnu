(ns jiksnu.modules.web.routes.user-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            jiksnu.modules.web.views.user-views
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(test-environment-fixture

 (future-fact "index page"
   (let [url "/users"
         response (response-for (req/request :get url))]
     response => map?
     (:status response) => status/success?
     (:body response) => string?))
 )
