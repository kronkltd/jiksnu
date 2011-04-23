(ns jiksnu.triggers.subscription-triggers-test
  (:use ciste.factory
        clj-tigase.core
        jiksnu.triggers.subscription-triggers
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe notify-subscribe-xmpp
  (do-it "should return a packet"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))
          response (notify-subscribe-xmpp {:id "JIKSNU1"} subscription)]
      (expect (packet? response)))))

(describe notify-unsubscribe-xmpp)

(describe notify-subscribe)

(describe notify-unsubscribe)
