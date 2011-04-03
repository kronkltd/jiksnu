(ns jiksnu.actions.activity-actions-test
  (:use ciste.factory
        ciste.sections
        ciste.view
        clj-tigase.core
        jiksnu.actions.activity-actions
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
      (let [response (index)]
        (expect (empty? response)))))
  (testing "when there are activities"
    (do-it "should return a seq of activities"
      (let [author (model.user/create (factory User))]
        (with-user author
          (model.activity/create (factory Activity))))
      (let [response (index)]
        (expect (seq response))
        (expect (every? activity? response))))))

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
                     response (create activity)]
                 (expect (activity? response)))))))))))

(describe new)

(describe show)

(describe update)

(describe delete)

(describe edit)

(describe create-activity)

(describe remote-create)

(describe fetch-comments)
