(ns jiksnu.xmpp.view.activity-view-test
  (:use ciste.view
        jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.view
        jiksnu.xmpp.view
        jiksnu.xmpp.view.activity-view
        [lazytest.describe :only (describe it testing do-it given for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as activity])
  (:import jiksnu.model.Activity))

#_(describe minimal-response
  (do-it "should return a sequence of elements"
    (with-database
      (let [packet (mock-activity-query-request-packet)
            activities (activity/index)
            response (minimal-response activities)]
        (expect (every? element? response))))))

#_(describe full-response
  (do-it "should return a sequence of elements"
    (with-database
      (given [packet (mock-activity-query-request-packet)
              entries (activity/index)
              response (full-response entries)]
        (expect (not (nil? response)))
        (expect (every? element? response))))))

(describe notify
  (do-it "should return a packet"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
             response (notify "daniel@renfer.name" activity)]
         (expect (packet? response))

         )))
    )
  )
