(ns jiksnu.xmpp.controller.subscription-controller-test
  (:use [jiksnu.factory :only (factory)]
        [jiksnu.mock :only (mock-subscriber-query-request-packet
                            mock-subscription-query-request-packet)]
        [jiksnu.model :only (with-database)]
        jiksnu.xmpp.controller.subscription-controller
        [jiksnu.xmpp.view :only (make-request)]
        [lazytest.describe :only (describe do-it it testing given)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription))

(describe subscribers
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (with-database
        (let [packet (mock-subscriber-query-request-packet)
              request (make-request packet)]
          (model.subscription/create
           (factory Subscription
                    {:from (.getLocalpart (:to request))}))
          (let [response (subscribers request)]
            (expect (every? (partial instance? Subscription) response))
            (expect (seq response))))))))

(describe subscriptions
  (testing "when there are subscriptions"
    (do-it "should return a sequence of subscriptions"
      (with-database
        (let [packet (mock-subscription-query-request-packet)
              user (model.user/create (factory User))
              request (assoc (make-request packet)
                        :to (:_id user))
              s (model.subscription/create
                 (factory Subscription
                          {:to (:_id user)}))
              results (subscriptions request)]
          (expect (not (empty? results)))
          (expect (every? (partial instance? Subscription) results)))))))
