(ns jiksnu.modules.xmpp.triggers.subscription-triggers-test
  (:use [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.modules.xmpp.triggers.subscription-triggers :only [notify-subscribe-xmpp]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]))

(test-environment-fixture

 (future-context #'notify-subscribe-xmpp
   (context "should return a packet"
     (let [subscription (mock/a-subscription-exists)]
       (notify-subscribe-xmpp {:id "JIKSNU1"} subscription) => packet/packet?))))
