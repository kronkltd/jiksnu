(ns jiksnu.mock-test
  (:use [clj-factory.core :only [fseq]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.mock :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.FeedSubscription
           jiksnu.model.Group
           jiksnu.model.Resource
           jiksnu.model.Subscription
           jiksnu.model.User))

(test-environment-fixture

 (context #'mock/activity-gets-posted
   (mock/activity-gets-posted) => (partial instance? Activity))

 (context #'mock/a-domain-exists
   (mock/a-domain-exists) => (partial instance? Domain))

 (context #'mock/a-resource-exists
   (mock/a-resource-exists) => (partial instance? Resource))

 (context #'mock/a-user-exists
   (context "without any params"
     (mock/a-user-exists) => (partial instance? User)))

 (context #'mock/a-feed-source-exists
   (mock/a-feed-source-exists) => (partial instance? FeedSource))

 (context #'mock/a-conversation-exists
   (mock/a-conversation-exists) => (partial instance? Conversation))

 (context #'mock/a-feed-subscription-exists
   (mock/a-feed-subscription-exists) => (partial instance? FeedSubscription))

 (context #'mock/a-subscription-exists
   (mock/a-subscription-exists) => (partial instance? Subscription))

 (context #'mock/a-group-exists
   (mock/a-group-exists) => (partial instance? Group))

 )
