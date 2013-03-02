(ns jiksnu.actions.pubsub-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [hiccup->doc test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker truthy anything]])
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

 (fact "#'subscribe"
   (let [params {:verify "async"}]
     (actions.pubsub/subscribe params) => .response.
     (provided
       (actions.pubsub/verify-subscription-async .subscription. params) => .response.)))

 (fact "#'hub-dispatch"
   (let [params {:mode "subscribe"}]
     (actions.pubsub/hub-dispatch params) => .response.
     (provided
       (actions.pubsub/subscribe params) => .response.)))

 )
