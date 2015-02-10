(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(test-environment-fixture

 (future-fact "index page (:viewmodel)"
   (let [response (->> "/main/conversations.viewmodel"
                       (req/request :get)
                       response-for)]
     response => map?
     (:status response) => status/success?
     (:body response) => string?))

 )
