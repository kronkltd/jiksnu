(ns jiksnu.mock-test
  (:use [clj-factory.core :only [fseq]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=> every-checker fact]])
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
   (mock/activity-gets-posted) => (partial instance? Activity)
   )

 (fact

   (mock/a-domain-exists) => (partial instance? Domain)
   (mock/a-resource-exists) => (partial instance? Resource)


   (context #'mock/a-user-exists

     (context "without any params"
       (mock/a-user-exists) => (partial instance? User))

     )

   (mock/a-feed-source-exists) => (partial instance? FeedSource)
   (mock/a-conversation-exists) => (partial instance? Conversation)
   (mock/a-feed-subscription-exists) => (partial instance? FeedSubscription)
   (mock/a-subscription-exists) => (partial instance? Subscription)
   (mock/a-group-exists) => (partial instance? Group)



   )

 )
