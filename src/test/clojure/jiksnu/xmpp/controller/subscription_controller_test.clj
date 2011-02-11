(ns jiksnu.xmpp.controller.subscription-controller-test
  (:use [jiksnu.factory :only (factory)]
        [jiksnu.mock :only (mock-subscriber-query-request-packet
                            mock-subscription-query-request-packet)]
        [jiksnu.model :only (with-database)]
        jiksnu.xmpp.controller.subscription-controller
        [jiksnu.xmpp.view :only (make-request)]
        [lazytest.describe :only (describe do-it it testing given)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as subscription.model])
  (:import jiksnu.model.Subscription))

(describe subscribers
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (with-database
        (let [packet (mock-subscriber-query-request-packet)
              request (make-request packet)]
          (subscription.model/create
           (factory Subscription
                    {:from (.getLocalpart (:to request))}))
          (let [response (subscribers request)]
            (expect (every? (partial instance? Subscription) response))
            (expect (seq response))))))))

(describe subscriptions
  (given [packet (mock-subscription-query-request-packet)
          request (make-request packet)]
    (testing "when there are subscriptions"
      (it "should not be empty"
        (with-database
          (let [s (factory Subscription
                           {:to (.getLocalpart (:to request))})]
            (subscription.model/create s))
          (let [results (subscriptions request)]
            (do
              (not (empty? results))))))
      (it "should return a sequence of subscriptions"
        (with-database
          (let [results (subscriptions request)]
            (every? (partial instance? Subscription) results)))))))
