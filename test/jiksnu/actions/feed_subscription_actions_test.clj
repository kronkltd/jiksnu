(ns jiksnu.actions.feed-subscription-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.feed-subscription-actions :refer [create delete exists? index
                                                              prepare-create subscription-request]]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.mock :as mock]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> fact falsey]])
  (:import jiksnu.model.FeedSubscription))

(test-environment-fixture

 (fact #'delete
   (let [item (mock/a-feed-subscription-exists)]
     (delete item)

     (exists? item) => falsey))

 (fact #'create
   (let [params (prepare-create (factory :feed-subscription))]
     (create params) => (partial instance? FeedSubscription)))

 (fact #'index
   (model.feed-subscription/drop!)
   (:items (index)) => [])

 (fact #'subscription-request
   (let [topic (fseq :uri)
         source (mock/a-feed-source-exists {:local true})
         params {:callback (fseq :uri)
                 :verify-token (fseq :verify-token)
                 :lease-seconds (fseq :lease-seconds)
                 :secret (fseq :secret-key)
                 :topic (:topic source)}]
     (subscription-request params)) => (partial instance? FeedSubscription))

 )
