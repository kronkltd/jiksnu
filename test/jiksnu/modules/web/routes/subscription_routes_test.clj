(ns jiksnu.modules.web.routes.subscription-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.ops :as ops]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [manifold.deferred :as d]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(future-fact "route: ostatus/sub :post"
  (let [username (fseq :username)
        domain-name (fseq :domain)
        uri (format "acct:%s@%s" username domain-name)
        params {:profile uri}]

    (fact "when not authenticated"
      (fact "when it is a remote user"
        (let [response (-> (req/request :post "/main/ostatussub")
                           (assoc :params params)
                           response-for)]
          response => map?
          (:status response) => status/redirect?)))

    (fact "when authenticated"
      (let [actor (mock/a-user-exists)]
        (fact "when it is a remote user"
          (-> (req/request :post "/main/ostatussub")
              (assoc :params params)
              (as-user actor)
              response-for) =>
          (contains {:status status/redirect?})
          (provided
           (ops/get-discovered anything) => (d/success-deferred
                                             (model/map->Domain {:_id domain-name}))))))))
