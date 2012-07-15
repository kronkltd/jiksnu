(ns jiksnu.triggers.activity-triggers-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        jiksnu.test-helper
        jiksnu.model
        [jiksnu.session :only [with-user]]
        jiksnu.triggers.activity-triggers
        jiksnu.views.activity-views
        midje.sweet)
  (require [clj-tigase.packet :as packet]
           [clojure.tools.logging :as log]
           [jiksnu.model.activity :as model.activity]
           [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'notify-activity"
   (fact "should return a packet"
     (let [user (model.user/create (factory :local-user))]
       (let [activity (model.activity/create
                       (factory :activity
                                {:author (:_id user)}))]
         (notify-activity user activity) => packet/packet?)))))
