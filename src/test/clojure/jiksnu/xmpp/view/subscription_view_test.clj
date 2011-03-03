(ns jiksnu.xmpp.view.subscription-view-test
  (:use jiksnu.core-test
        jiksnu.factory
        jiksnu.view
        jiksnu.xmpp.view
        jiksnu.xmpp.view.subscription-view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
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

#_(describe subscription-request-minimal {:focus true}
    (do-it "should"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            subscription (model.subscription/subscribe
                          (:_id user) (:_id subscribee))]
        (let [response (subscription-request-minimal subscription)]
          (expect (element? response))
          (println "response: " response)))))

(describe notify-subscribe
  (do-it "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (notify-subscribe {} subscription)]
        (expect (packet? response))
        (println "response: " response)))))

(describe subscriber-response-minimal)

(describe index)
