(ns jiksnu.triggers.activity-triggers-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.triggers.activity-triggers :only [notify-activity]]
        [midje.sweet :only [fact =>]])
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (fact "#'notify-activity"
   (let [user (mock/a-user-exists)
         activity (mock/there-is-an-activity {:user user})]
     (notify-activity user activity) => packet/packet?))

 )
