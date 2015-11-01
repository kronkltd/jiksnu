(ns jiksnu.modules.web.routes.group-routes-test
  (:require [ciste.model :as cm]
            [ciste.sections.default :refer [uri]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            jiksnu.modules.web.routes.group-routes
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "route: group-api/collection :get"
  (let [url (str "/model/groups")
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status 200})))

(facts "route: group-api/collection :post"
  (let [params (factory :group)
        url (str "/model/groups")
        request (assoc (req/request :post url)
                       :non-query-params params)]
    (prn params)
    (response-for request) =>
    (contains
     {:status 201
      :body (contains {:name (:name params)})
      })))

(facts "route: group-api/item :get"
  (let [group (mock/a-group-exists)
        url (str "/model/groups/" (:_id group))
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status 200})))
