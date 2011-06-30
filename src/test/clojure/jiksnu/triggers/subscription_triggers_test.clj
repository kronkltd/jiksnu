(ns jiksnu.triggers.subscription-triggers-test
  (:use clj-factory.core
        clj-tigase.core
        jiksnu.triggers.subscription-triggers)
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe notify-subscribe-xmpp
  (testing "should return a packet"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))
          response (notify-subscribe-xmpp {:id "JIKSNU1"} subscription)]
      (expect (packet? response)))))
