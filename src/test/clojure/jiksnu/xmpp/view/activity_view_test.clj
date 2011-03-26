(ns jiksnu.xmpp.view.activity-view-test
  (:use clj-tigase.core
        ciste.factory
        ciste.sections
        ciste.view
        jiksnu.model
        jiksnu.session
        jiksnu.view
        jiksnu.xmpp.view
        jiksnu.xmpp.view.activity-view
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe show-section
  (do-it "should return a sequence of elements"
    (let [packet nil
          entries (model.activity/index)
          response (show-section entries)]
      (expect (not (nil? response)))
      (expect (every? element? response)))))

(describe notify-activity
  (do-it "should return a packet"
    (with-serialization :xmpp
      (with-format :atom
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (model.activity/create (factory Activity))
                  response (notify-activity user activity)]
              (expect (packet? response)))))))))
