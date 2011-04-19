(ns jiksnu.triggers.subscription-triggers-test)

(describe notify-subscribe-xmpp
  (do-it "should return a packet"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))
          response (notify-subscribe {:id "JIKSNU1"} subscription)]
      (expect (packet? response)))))

(describe notify-unsubscribe-xmpp)

(describe notify-subscribe)

(describe notify-unsubscribe)
