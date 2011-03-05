(ns jiksnu.xmpp.view.subscription-view-test
  (:use jiksnu.core-test
        jiksnu.factory
        jiksnu.view
        jiksnu.xmpp.view
        jiksnu.xmpp.view.subscription-view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.xmpp.controller.subscription-controller :as
             controller.subscription])
  (:import jiksnu.model.User))

(describe subscriber-response-element
  (do-it "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscriber-response-element subscription)]
        (expect (element? response))
        (println "response: " response)))))

(describe subscription-request-minimal
    (do-it "should"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            subscription (model.subscription/subscribe
                          (:_id user) (:_id subscribee))]
        (let [response (subscription-request-minimal subscription)]
          (expect (element? response))
          (println "response: " response)))))

(describe unsubscription-request-minimal)

(describe minimal-subscriber-response)

(describe subscriber-response-element)

(describe minimal-subscription-response)

(describe controller.subscription/subscriptions ":xmpp")

(describe controller.subscription/subscribers ":xmpp")

(describe controller.subscription/subscribe ":xmpp")

(describe notify-subscribe
  (do-it "should return a packet"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))
          response (notify-subscribe {} subscription)]
      (expect (packet? response)))))

(describe notify-unsubscribe)

(describe controller.subscription/unsubscribe ":xmpp")

(describe controller.subscription/subscribed ":xmpp")

(describe controller.subscription/remote-subscribe-confirm ":xmpp")
