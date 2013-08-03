(ns jiksnu.actions.pubsub-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [context hiccup->doc test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.features-helper :as feature]
            [jiksnu.actions.activity-actions :as actions.activity]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context #'actions.pubsub/subscribe
   (let [params {:verify "async"}]
     (actions.pubsub/subscribe params) => .response.
     (provided
       (actions.feed-subscription/subscription-request params) => .subscription.
       (actions.pubsub/verify-subscription-async .subscription. params) => .response.)))

 (context #'actions.pubsub/hub-dispatch
   (let [params {:mode "subscribe"}]
     (actions.pubsub/hub-dispatch params) => .response.
     (provided
       (actions.pubsub/subscribe params) => .response.)))

 )
