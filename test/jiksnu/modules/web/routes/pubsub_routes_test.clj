(ns jiksnu.modules.web.routes.pubsub-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.factory :as f]
            [jiksnu.helpers.routes :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(future-fact "route: subscriptions-api/item :post"
  (let [domain (mock/a-domain-exists)
        source (mock/a-feed-source-exists
                {:domain (actions.domain/current-domain)})
        topic-url (:topic source)
        callback-url (f/make-uri (:_id domain))
        params {"hub.topic"        topic-url
                "hub.secret"       (fseq :secret-key)
                "hub.verify_token" (fseq :verify-token)
                "hub.verify"       "sync"
                "hub.callback"     callback-url
                "hub.mode"         "subscribe"}]

    (-> (req/request :post "/main/push/hub")
        (assoc :params params)
        response-for) =>
    (contains {:status 204})
    (provided
     (actions.pubsub/verify-subscribe-sync anything anything) => true)))
