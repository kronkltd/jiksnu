(ns jiksnu.actions.feed-subscription-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.mock :as mock]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.FeedSubscription))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.feed-subscription/delete"
  (let [item (mock/a-feed-subscription-exists)]
    (actions.feed-subscription/delete item)

    (actions.feed-subscription/exists? item) => falsey))

(fact "#'actions.feed-subscription/create"
  (let [params (factory :feed-subscription)
        params (actions.feed-subscription/prepare-create params)]

    (actions.feed-subscription/create params {})
    => (partial instance? FeedSubscription)))

(fact "#'actions.feed-subscription/index"
  (model.feed-subscription/drop!)

  (actions.feed-subscription/index) => (contains {:items []}))

(fact "#'actions.feed-subscription/subscription-request"
  (let [topic (fseq :uri)
        source (mock/a-feed-source-exists {:local true})
        params {:callback (fseq :uri)
                :verify-token (fseq :verify-token)
                :lease-seconds (fseq :lease-seconds)
                :secret (fseq :secret-key)
                :topic (:topic source)}]

    (actions.feed-subscription/subscription-request params)
    => (partial instance? FeedSubscription)))
