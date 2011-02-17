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
      (with-environment :test
        (model.user/drop!)
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:from user
                       :to user
                       :body (mock-vcard-query-request-element)})
              request (make-request packet)
              response (show request)]
          (expect (instance? User response))
          (expect (= response user)))))))

#_(describe create
  (given [packet (mock-vcard-publish-request-packet)
          request (make-request packet)]
    (it "should not be nil" :pending
      (with-environment :test
        (let [response (create request)]
          (not (nil? response)))))))

(describe delete)

(describe inbox
  (given [request (make-request (mock-inbox-query-request-packet))]
    (testing "when there are no activities"
      (do-it "should be empty"
        (with-environment :test
          (model.activity/drop!)
          (let [response (inbox request)]
            (expect (empty? response))))))
    (testing "when there are activities"
      (do-it "should return a seq of activities"
        (with-environment :test
          (model.activity/drop!)
          (let [author (model.user/create (factory User))]
            (with-user author
              (model.activity/create (factory Activity))))
          (let [response (inbox request)]
            (expect (seq response))
            (expect (every? #(instance? Activity %) response))))))))
