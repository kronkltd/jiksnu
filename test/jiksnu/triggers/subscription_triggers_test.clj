(ns jiksnu.triggers.subscription-triggers-test
  (:use (ciste [config :only [with-environment]])
        clj-factory.core
        clojure.test
        jiksnu.test-helper
        jiksnu.triggers.subscription-triggers
        midje.sweet)
  (:require [clj-tigase.packet :as packet]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(with-environment :test
  (test-environment-fixture)

  (fact "notify-subscribe-xmpp"
    (fact "should return a packet"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            subscription (model.subscription/subscribe
                          (:_id user) (:_id subscribee))
            response (notify-subscribe-xmpp {:id "JIKSNU1"} subscription)]
        (is (packet/packet? response))))))
