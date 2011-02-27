(ns jiksnu.xmpp.controller.subscription-controller-test
  (:use [jiksnu.factory :only (factory)]
        [jiksnu.mock :only (mock-subscriber-query-request-element
                            mock-subscription-query-request-element)]
        jiksnu.model
        jiksnu.xmpp.controller.subscription-controller
        [jiksnu.xmpp.view :only (make-request make-jid make-packet)]
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(describe subscribers
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (let [user (model.user/create (factory User))
            subscriber (model.user/create (factory User))
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :body (mock-subscriber-query-request-element)})
            request (make-request packet)
            s (model.subscription/create
               (factory Subscription
                        {:from (:_id subscriber)
                         :to (:_id user)}))
            response (subscribers request)]
        (expect (seq response))
        (expect (every? (partial instance? Subscription) response))))))

(describe subscriptions
  (testing "when there are subscriptions"
    (do-it "should return a sequence of subscriptions"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :body (mock-subscription-query-request-element)})
            request (make-request packet)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id user)
                                    :to (:_id subscribee)}))
            results (subscriptions request)]
        (expect (not (empty? results)))
        (expect (every? (partial instance? Subscription) results))))))
