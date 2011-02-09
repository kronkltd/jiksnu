(ns jiksnu.xmpp.controller.user-controller-test
  (:use jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.session
        jiksnu.xmpp.controller.user-controller
        jiksnu.xmpp.view
        [lazytest.describe :only (describe it do-it testing given for-any)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe rule-element?)

(describe rule-map)

(describe property-map)

(describe show
  (testing "when the user exists"
    (do-it "should return that user"
      (with-database
        (let [user (model.user/create (factory User))
              packet (mock-vcard-query-request-packet)
              mock-request (make-request packet)
              request (assoc mock-request
                        :to (make-jid (:_id user) (:domain user)))
              response (show request)]
          (expect (instance? User response))
          (expect (= response user)))))))

(describe create
  (given [packet (mock-vcard-publish-request-packet)
          request (make-request packet)]
    (it "should not be nil" :pending
      (with-database
        (let [response (create request)]
          (not (nil? response)))))))

(describe delete)

(describe inbox
  (given [request (make-request (mock-inbox-query-request-packet))]
    (testing "when there are no activities"
      (do-it "should be empty"
        (with-database
          (model.activity/drop!)
          (let [response (inbox request)]
            (expect (empty? response))))))
    (testing "when there are activities"
      (do-it "should return a seq of activities"
        (with-database
          (model.activity/drop!)
          (let [author (model.user/create (factory User))]
            (with-user (:_id author)
              (model.activity/create (factory Activity))))
          (let [response (inbox request)]
            (expect (seq response))
            (expect (every? #(instance? Activity %) response))))))))
