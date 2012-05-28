(ns jiksnu.triggers.activity-triggers-test
  (:use [ciste.config :only [with-environment]]
        [ciste.debug :only [spy]]
        [clj-factory.core :only [factory]]
        jiksnu.test-helper
        jiksnu.model
        [jiksnu.session :only [with-user]]
        jiksnu.triggers.activity-triggers
        jiksnu.views.activity-views
        midje.sweet)
  (require [clj-tigase.packet :as packet]
           [jiksnu.model.activity :as model.activity]
           [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'notify-activity"
   (fact "should return a packet"
     (let [user (model.user/create (factory User))]
       (with-user user
         (let [activity (model.activity/create
                         (factory Activity
                                  {:author (:_id user)}))]
           (notify-activity user activity) => packet/packet?))))))
