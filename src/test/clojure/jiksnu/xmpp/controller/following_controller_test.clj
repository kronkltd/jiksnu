(ns jiksnu.xmpp.controller.following-controller-test
  (:use jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.xmpp.controller.following-controller
        jiksnu.xmpp.view
        [lazytest.describe :only (describe it testing given)])
  (:require [jiksnu.model.following :as following.model]
            [jiksnu.model.subscription :as subscription.model])
  (:import jiksnu.model.Subscription))

(describe index
  (given [packet (mock-subscription-query-request-packet)
          request (make-request packet)]
    (testing "when there are subscriptions"
      (it "should not be empty"
        (with-database
          (let [s (factory Subscription
                           {:to (.getLocalpart (:to request))})]
            (subscription.model/create s))
          (let [results (index request)]
            (do
              (not (empty? results))))))
      (it "should return a sequence of subscriptions"
        (with-database
          (let [results (index request)]
            (every? (partial instance? Subscription) results)))))))
