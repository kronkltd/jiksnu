(ns jiksnu.triggers.activity-triggers-test
  (:use ciste.core
        ciste.debug
        ciste.views
        clj-factory.core
        clj-tigase.core
        jiksnu.model
        jiksnu.session
        jiksnu.triggers.activity-triggers
        jiksnu.views.activity-views
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (require [jiksnu.model.activity :as model.activity]
           [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe notify-activity
  (do-it "should return a packet"
    (let [user (model.user/create (factory User))]
      (with-user user
        (let [activity (model.activity/create
                        (factory Activity
                                 {:authors [(:_id user)]}))
              response (notify-activity user activity)]
          (expect (packet? response)))))))
