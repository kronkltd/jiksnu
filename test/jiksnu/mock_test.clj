(ns jiksnu.mock-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Activity
           jiksnu.model.Album
           jiksnu.model.Client
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.FeedSubscription
           jiksnu.model.Group
           jiksnu.model.Like
           jiksnu.model.Notification
           jiksnu.model.Picture
           jiksnu.model.RequestToken
           jiksnu.model.Resource
           jiksnu.model.Service
           jiksnu.model.Stream
           jiksnu.model.Subscription
           jiksnu.model.User))

(th/module-test ["jiksnu.modules.core"])

(fact "#'mock/activity-gets-posted"
  (mock/activity-gets-posted) => (partial instance? Activity))

(fact "#'mock/a-domain-exists"
  (mock/a-domain-exists) => (partial instance? Domain))

(fact "#'mock/a-resource-exists"
  (mock/a-resource-exists) => (partial instance? Resource))

(fact "#'mock/a-client-exists"
  (mock/a-client-exists) => (partial instance? Client))

(fact "#'mock/a-like-exists"
  (mock/a-like-exists) => (partial instance? Like))

(fact "#'mock/a-notification-exists"
  (mock/a-notification-exists) => (partial instance? Notification))

(fact "#'mock/a-user-exists"
  (fact "without any params"
    (mock/a-user-exists) => (partial instance? User)))

(fact "#'mock/a-stream-exists"
  (fact "without any params"
    (mock/a-stream-exists) => (partial instance? Stream)))

(fact "#'mock/a-feed-source-exists"
  (mock/a-feed-source-exists) => (partial instance? FeedSource))

(fact "#'mock/a-service-exists"
  (mock/a-service-exists) => (partial instance? Service))

(fact "#'mock/a-conversation-exists"
  (mock/a-conversation-exists) => (partial instance? Conversation))

(fact "#'mock/a-feed-subscription-exists"
  (mock/a-feed-subscription-exists) => (partial instance? FeedSubscription))

(fact "#'mock/a-subscription-exists"
  (mock/a-subscription-exists) => (partial instance? Subscription))

(fact "#'mock/a-group-exists"
  (mock/a-group-exists) => (partial instance? Group))

(fact "#'mock/an-album-exists"
  (mock/an-album-exists) => (partial instance? Album))

(fact "#'mock/a-request-token-exists"
  (mock/a-request-token-exists) => (partial instance? RequestToken))

(fact "#'mock/a-picture-exists"
  (mock/a-picture-exists) => (partial instance? Picture))
