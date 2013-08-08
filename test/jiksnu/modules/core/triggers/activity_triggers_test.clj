(ns jiksnu.modules.core.triggers.activity-triggers-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [jiksnu.modules.core.triggers.activity-triggers :only [notify-activity]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]))

(test-environment-fixture

 (context #'notify-activity
   (let [user (mock/a-user-exists)
         activity (mock/there-is-an-activity {:user user})]
     (notify-activity user activity) => packet/packet?))

 )
