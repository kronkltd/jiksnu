(ns jiksnu.modules.core.actions.pubsub-actions-test
  (:require [jiksnu.modules.core.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.modules.core.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.pubsub/subscribe"
  (let [params {:verify "async"}]
    (actions.pubsub/subscribe params) => .response.
    (provided
     (actions.feed-subscription/subscription-request params) => .subscription.
     (actions.pubsub/verify-subscription-async .subscription. params) => .response.)))

(fact "#'actions.pubsub/hub-dispatch"
  (let [params {:mode "subscribe"}]
    (actions.pubsub/hub-dispatch params) => .response.
    (provided
     (actions.pubsub/subscribe params) => .response.)))
