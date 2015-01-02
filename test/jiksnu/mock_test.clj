(ns jiksnu.mock-test
  (:require [clj-factory.core :refer [fseq]]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact]])
  (:import jiksnu.model.Activity
           jiksnu.model.Client
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.FeedSubscription
           jiksnu.model.Group
           jiksnu.model.RequestToken
           jiksnu.model.Resource
           jiksnu.model.Stream
           jiksnu.model.Subscription
           jiksnu.model.User))

(test-environment-fixture

 (fact #'mock/activity-gets-posted
   (mock/activity-gets-posted) => (partial instance? Activity))

 (fact #'mock/a-domain-exists
   (mock/a-domain-exists) => (partial instance? Domain))

 (fact #'mock/a-resource-exists
   (mock/a-resource-exists) => (partial instance? Resource))

 (fact #'mock/a-client-exists
   (mock/a-client-exists) => (partial instance? Client))

 (fact #'mock/a-user-exists
   (fact "without any params"
     (mock/a-user-exists) => (partial instance? User)))

 (fact #'mock/a-stream-exists
   (fact "without any params"
     (mock/a-stream-exists) => (partial instance? Stream)))

 (fact #'mock/a-feed-source-exists
   (mock/a-feed-source-exists) => (partial instance? FeedSource))

 (fact #'mock/a-conversation-exists
   (mock/a-conversation-exists) => (partial instance? Conversation))

 (fact #'mock/a-feed-subscription-exists
   (mock/a-feed-subscription-exists) => (partial instance? FeedSubscription))

 (fact #'mock/a-subscription-exists
   (mock/a-subscription-exists) => (partial instance? Subscription))

 (fact #'mock/a-group-exists
   (mock/a-group-exists) => (partial instance? Group))

 (fact #'mock/a-request-token-exists
   (mock/a-request-token-exists) => (partial instance? RequestToken))

 )
