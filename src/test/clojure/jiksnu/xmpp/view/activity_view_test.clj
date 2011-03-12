(ns jiksnu.xmpp.view.activity-view-test
  (:use ciste.view
        jiksnu.factory
        jiksnu.mock
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

#_(describe minimal-response
  (do-it "should return a sequence of elements"
    (let [packet (mock-activity-query-request-packet)
          activities (model.activity/index)
          response (minimal-response activities)]
      (expect (every? element? response)))))

#_(describe full-response
  (do-it "should return a sequence of elements"
    (let [packet (mock-activity-query-request-packet)
          entries (model.activity/index)
          response (full-response entries)]
      (expect (not (nil? response)))
      (expect (every? element? response)))))

#_(describe notify
  (do-it "should return a packet"
    (with-serialization :xmpp
      (with-format :atom
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (model.activity/create (factory Activity))
                  response (notify user activity)]
              (expect (packet? response)))))))))
