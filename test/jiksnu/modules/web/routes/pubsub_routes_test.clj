(ns jiksnu.modules.web.routes.pubsub-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [check context future-context
                                        hiccup->doc test-environment-fixture]]
            [midje.sweet :refer [=> anything]]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "subscription request"
   (let [domain (mock/a-domain-exists)
         source (mock/a-feed-source-exists
                 {:domain (actions.domain/current-domain)})
         topic-url (:topic source)
         callback-url (factory/make-uri (:_id domain))
         params {"hub.topic"        topic-url
                 "hub.secret"       (fseq :secret-key)
                 "hub.verify_token" (fseq :verify-token)
                 "hub.verify"       "sync"
                 "hub.callback"     callback-url
                 "hub.mode"         "subscribe"}]

     (-> (req/request :post "/main/push/hub")
         (assoc :params params)
         response-for) =>
         (check [response]
           response => map?
           (:status response) => 204)
         (provided
           (actions.pubsub/verify-subscribe-sync anything anything) => true)))

 )
