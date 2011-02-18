(ns jiksnu.xmpp.controller.subscription-controller-test
  (:use [jiksnu.factory :only (factory)]
        [jiksnu.mock :only (mock-subscriber-query-request-element
                            mock-subscription-query-request-element)]
        jiksnu.model
        jiksnu.xmpp.controller.subscription-controller
        [jiksnu.xmpp.view :only (make-request make-jid make-packet)]
        [lazytest.describe :only (describe do-it it testing given)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(describe subscribers
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (with-environment :test
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to user
                       :from user
                       :body (mock-subscriber-query-request-element)})
              request (make-request packet)
              s (model.subscription/create
                 (factory Subscription
                          {:to (.getLocalpart (:to request))}))
              response (subscribers request)]
          (expect (every? (partial instance? Subscription) response))
          (expect (seq response)))))))

(describe subscriptions
  (testing "when there are subscriptions"
    (do-it "should return a sequence of subscriptions"
      (with-environment :test
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to user
                       :from user
                       :body (mock-subscription-query-request-element)})
              request (make-request packet)
              s (model.subscription/create
                 (factory Subscription
                          {:from (:_id user)}))
              results (subscriptions request)]
          (expect (not (empty? results)))
          (expect (every? (partial instance? Subscription) results)))))))
