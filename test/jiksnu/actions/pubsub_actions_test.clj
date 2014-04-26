(ns jiksnu.actions.pubsub-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [context hiccup->doc test-environment-fixture]]
            [midje.sweet :refer [=>]]
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
