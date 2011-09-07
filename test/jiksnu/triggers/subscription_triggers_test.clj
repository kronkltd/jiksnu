(ns jiksnu.triggers.subscription-triggers-test
  (:use clj-factory.core
        clojure.test
        jiksnu.core-test
        jiksnu.triggers.subscription-triggers)
  (:require [clj-tigase.packet :as packet]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest notify-subscribe-xmpp-test
  (testing "should return a packet"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))
          response (notify-subscribe-xmpp {:id "JIKSNU1"} subscription)]
      (is (packet/packet? response)))))
