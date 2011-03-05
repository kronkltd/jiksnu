(ns jiksnu.xmpp.controller.subscription-controller-test
  (:use [jiksnu.factory :only (factory)]
        [jiksnu.mock :only (mock-subscriber-query-request-element
                            mock-subscription-query-request-element)]
        jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp.controller.subscription-controller
        [jiksnu.xmpp.view :only (make-request
                                 make-jid make-packet make-element)]
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(describe subscriptions
  (testing "when there are subscriptions"
    (do-it "should return a sequence of subscriptions"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            element (make-element
                     "pubsub" {"xmlns" pubsub-uri}
                     ["subscriptions" {"node" microblog-uri}])
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :type :get
                     :body element})
            request (make-request packet)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id user)
                                    :to (:_id subscribee)}))
            results (subscriptions request)]
        (expect (not (empty? results)))
        (expect (every? (partial instance? Subscription) results))))))

(describe subscribers {:focus true}
  (testing "when there are subscribers"
    (do-it "should not be empty"
      (let [user (model.user/create (factory User))
            subscriber (model.user/create (factory User))
            element (make-element
                     "pubsub" {"xmlns" pubsub-uri}
                     ["subscribers" {"node" microblog-uri}])
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :type :get
                     :body element})
            request (make-request packet)
            subscription (model.subscription/create
                          (factory Subscription
                                   {:from (:_id subscriber)
                                    :to (:_id user)}))
            response (subscribers request)]
        (expect (seq response))
        (expect (every? (partial instance? Subscription) response))))))

(describe subscribe)

(describe remote-subscribe)

(describe remote-subscribe-confirm)

(describe subscribed)

(describe unsubscribe)
