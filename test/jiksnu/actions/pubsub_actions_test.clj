(ns jiksnu.actions.pubsub-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

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
