(ns jiksnu.xmpp.controller.follower-controller-test
  (:use [jiksnu.factory :only (factory)]
        [jiksnu.mock :only (mock-subscriber-query-request-packet)]
        [jiksnu.model :only (with-database)]
        jiksnu.xmpp.controller.follower-controller
        [jiksnu.xmpp.view :only (make-request)]
        [lazytest.describe :only (describe do-it testing given)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as subscription.model])
  (:import jiksnu.model.Subscription))

(describe index
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (with-database
        (let [packet (mock-subscriber-query-request-packet)
              request (make-request packet)]
          (subscription.model/create
           (factory Subscription
                    {:from (.getLocalpart (:to request))}))
          (let [response (index request)]
            (expect (every? (partial instance? Subscription) response))
            (expect (seq response))))))))
