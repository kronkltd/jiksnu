(ns jiksnu.modules.web.routes.activity-routes-test
  (:require [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [fseq]]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            jiksnu.modules.web.routes.activity-routes
            [jiksnu.routes-helper :refer [as-user json-response response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import org.apache.http.HttpStatus))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: activities-api/item :delete"
  (fact "when authenticated"
    (let [user (mock/a-user-exists)
          activity (mock/an-activity-exists {:user user})
          url (str "/model/activities/" (:_id activity))
          request (-> (req/request :delete url)
                      (as-user user))]
      (response-for request) => (contains {:status HttpStatus/SC_NO_CONTENT})
      (model.activity/fetch-by-id (:_id activity)) => nil))
  (fact "when not authenticated"
    (let [user (mock/a-user-exists)
          activity (mock/an-activity-exists {:user user})
          url (str "/model/activities/" (:_id activity))
          request (req/request :delete url)]
      (response-for request) => (contains {:status HttpStatus/SC_UNAUTHORIZED})
      (model.activity/fetch-by-id (:_id activity)) =not=> nil)))
