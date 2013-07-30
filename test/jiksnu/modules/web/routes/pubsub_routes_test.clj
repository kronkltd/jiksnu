(ns jiksnu.modules.web.routes.pubsub-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context
                                   hiccup->doc test-environment-fixture]]
        [midje.sweet :only [=> anything]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [net.cgrand.enlive-html :as enlive]
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

     (-> (req/request :post (named-path "hub dispatch"))
         (assoc :params params)
         response-for) =>
         (check [response]
           response => map?
           (:status response) => 204)
         (provided
           (actions.pubsub/verify-subscribe-sync anything anything) => true)))

 )
