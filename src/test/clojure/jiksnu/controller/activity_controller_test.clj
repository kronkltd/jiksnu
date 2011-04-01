(ns jiksnu.controller.activity-controller-test
  (:use ciste.factory
        ciste.sections
        ciste.view
        clj-tigase.core
        jiksnu.controller.activity-controller
        ciste.debug
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe index
  (testing "when there are no activities"
    (do-it "should be empty"
      (model.activity/drop!)
      (let [request {}
            response (index request)]
        (expect (empty? response)))))
  (testing "when there are activities"
    (do-it "should return a seq of activities"
      (let [author (model.user/create (factory User))]
        (with-user author
          (model.activity/create (factory Activity))))
      (let [request {}
            response (index request)]
        (expect (seq response))
        (expect (every? activity? response))))))

(describe create)

(describe new)

(describe show)

(describe update)

(describe delete)

(describe edit)

(describe index
  (testing "when there are no activities"
    (do-it "should return an empty sequence"
      (let [user (model.user/create (factory User))
            element nil
            packet (make-packet
                    {:from (make-jid user)
                     :to (make-jid user)
                     :type :get
                     :body element})
            request (make-request packet)]
        (let [response (index request)]
          (expect (not (nil? response)))
          (expect (empty? response))))))
  (testing "when there are activities"
    (do-it "should return a sequence of activities"
      (let [author (model.user/create (factory User))]
        (with-user author
          (let [element nil
                packet (make-packet
                        {:from (make-jid author)
                         :to (make-jid author)
                         :type :get
                         :id (fseq :id)
                         :body element})
                request (make-request packet)
                activity (model.activity/create (factory Activity))
                response (index request)]
            (expect (seq response))
            (expect (every? activity? response))))))))

(describe create-activity)

(describe create
  (testing "when the user is logged in"
    (testing "and it is a valid activity"
     (do-it "should return that activity"
       (with-serialization :xmpp
         (with-format :xmpp
           (let [user (model.user/create (factory User))]
             (with-user user
               (let [activity (factory Activity)
                     element (make-element
                              (index-section [activity]))
                     packet (make-packet
                             {:to (make-jid user)
                              :from (make-jid user)
                              :type :set
                              :body element})
                     request (make-request packet)
                     response (create request)]
                 (expect (activity? response)))))))))))

(describe remote-create)

(describe fetch-comments)
