(ns jiksnu.triggers.subscription-triggers-test
  (:use [ciste.config :only [with-environment]]
        clj-factory.core
        jiksnu.test-helper
        jiksnu.triggers.subscription-triggers
        midje.sweet)
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (future-fact "notify-subscribe-xmpp"
   (fact "should return a packet"
     (let [subscription (mock/a-subscription-exists)]
       (notify-subscribe-xmpp {:id "JIKSNU1"} subscription) => packet/packet?))))
