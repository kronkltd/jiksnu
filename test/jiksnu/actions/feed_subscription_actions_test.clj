(ns jiksnu.actions.feed-subscription-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.feed-subscription-actions :only [create delete exists? index
                                                         prepare-create subscription-request]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=> falsey]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSubscription))

(test-environment-fixture

 (context #'delete
   (let [item (mock/a-feed-subscription-exists)]
     (delete item)

     (exists? item) => falsey))

 (context #'create
   (let [params (prepare-create (factory :feed-subscription))]
     (create params) => (partial instance? FeedSubscription)))

 (context #'index
   (model.feed-subscription/drop!)
   (:items (index)) => [])

 (context #'subscription-request
   (let [topic (fseq :uri)
         source (mock/a-feed-source-exists {:local true})
         params {:callback (fseq :uri)
                 :verify-token (fseq :verify-token)
                 :lease-seconds (fseq :lease-seconds)
                 :secret (fseq :secret-key)
                 :topic (:topic source)}]
     (subscription-request params)) => (partial instance? FeedSubscription))

 )
