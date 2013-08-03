(ns jiksnu.modules.xmpp.triggers.subscription-triggers-test
  (:use [ciste.config :only [with-environment]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.modules.xmpp.triggers.subscription-triggers :only [notify-subscribe-xmpp]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature])
  (:import jiksnu.model.User))

(test-environment-fixture

 (future-context #'notify-subscribe-xmpp
   (context "should return a packet"
     (let [subscription (mock/a-subscription-exists)]
       (notify-subscribe-xmpp {:id "JIKSNU1"} subscription) => packet/packet?))))
