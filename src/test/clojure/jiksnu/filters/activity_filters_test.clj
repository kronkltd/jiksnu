(ns jiksnu.filters.activity-filters-test
  (:use jiksnu.filters.activity-filters
      jiksnu.model
        jiksnu.session
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)]
        )
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User)
  )

(describe show ":xmpp"
  (testing "when the activity exists"
    (do-it "should return that activity"
      (let [author (model.user/create (factory User))]
        (with-user author
          (let [activity (model.activity/create (factory Activity))
                packet-map {:from (make-jid author)
                            :to (make-jid author)
                            :type :get
                            :id "JIKSNU1"
                            :body (make-element
                                   "pubsub" {"xmlns" pubsub-uri}
                                   ["items" {"node" microblog-uri}
                                    ["item" {"id" (:_id activity)}]])}
                packet (make-packet packet-map)
                request (make-request packet)
                response (show request)]
            (expect (activity? response)))))))
  (testing "when the activity does not exist"
    (do-it "should return nil" :pending)))
