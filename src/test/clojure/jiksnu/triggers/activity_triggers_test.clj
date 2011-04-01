(ns jiksnu.triggers.activity-triggers-test
  (:use clj-tigase.core
        ciste.factory
        ciste.view
        jiksnu.triggers.activity-triggers
        jiksnu.xmpp.view.activity-view
        jiksnu.model
        jiksnu.session
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (require [jiksnu.model.activity :as model.activity]
           [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe notify-activity
  (do-it "should return a packet"
    (with-serialization :xmpp
      (with-format :atom
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (model.activity/create (factory Activity))
                  response (notify-activity user activity)]
              (expect (packet? response)))))))))
