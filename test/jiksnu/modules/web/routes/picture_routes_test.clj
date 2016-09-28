(ns jiksnu.modules.web.routes.picture-routes-test
  (:require [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: pictures-api/index :get"
  (fact "when there is a picture"
    (let [url "/model/pictures"]
      (let [picture  (mock/there-is-a-picture)
            response (util/inspect (response-for (req/request :get url)))]
        response =>
        (contains {:status status/success?
                   :body string?})
        (let [body (some-> response :body json/read-str)]
          body => (contains {"totalItems" 1}))))))
