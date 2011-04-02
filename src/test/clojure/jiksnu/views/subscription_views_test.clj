(ns jiksnu.views.subscription-views-test
  (:use ciste.core
        ciste.debug
        ciste.factory
        clj-tigase.core
        jiksnu.core-test
        jiksnu.namespace
        jiksnu.sections.subscription-sections
        jiksnu.view
        jiksnu.views.subscription-views
        jiksnu.xmpp.element
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.controller.subscription-controller :as
             controller.subscription])
  (:import jiksnu.model.User))

(describe subscriber-response-element
  (do-it "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscriber-response-element subscription)]
        (expect (or (vector? response)
                    (element? response)))))))

(describe subscription-request-minimal
    (do-it "should"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            subscription (model.subscription/subscribe
                          (:_id user) (:_id subscribee))]
        (let [response (subscription-request-minimal subscription)]
          (expect (or (vector? response)
                      (element? response)))))))

(describe unsubscription-request-minimal)

(describe subscriber-response-minimal)

(describe subscriber-response-element)

(describe subscription-response-minimal)

(describe controller.subscription/subscriptions ":xmpp")

(describe controller.subscription/subscribers ":xmpp")

(describe apply-view "#'controller.subscription/subscribe :xmpp")

(describe notify-subscribe
  (do-it "should return a packet"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))
          response (notify-subscribe {:id "JIKSNU1"} subscription)]
      (expect (packet? response)))))

(describe notify-unsubscribe)

(describe controller.subscription/unsubscribe ":xmpp"
  (testing "when there is no subscription"
    (do-it "should return a packet map"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            element (make-element
                     "pubsub" {"xmlns" pubsub-uri}
                     ["unsubscribe" {"node" microblog-uri}])
            packet (make-packet
                    {:to (make-jid subscribee)
                     :from (make-jid user)
                     :type :set
                     :body element})
            request (merge (make-request packet)
                           {:action #'controller.subscription/unsubscribe
                            :format :xmpp})
            record (controller.subscription/unsubscribe request)
            response (apply-view request record)]
        (expect (map? response))))))

(describe controller.subscription/subscribed ":xmpp")

(describe controller.subscription/remote-subscribe-confirm ":xmpp")
