(ns jiksnu.modules.web.routes.group-membership-routes-test
  (:require [ciste.model :as cm]
            [ciste.sections.default :refer [uri]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.routes.group-membership-routes
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: group-memberships-api/collection :get"
  (let [group-membership (mock/a-group-membership-exists)
        path "/model/group-memberships"
        request (req/request :get path)
        response (response-for request)]
    response => (contains {:status status/success?})
    (let [body (json/read-json (:body response))]
      body => (contains {:totalItems 1}))))
