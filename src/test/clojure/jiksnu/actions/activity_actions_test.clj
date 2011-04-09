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

(describe delete
  (testing "when the activity exists"
    (testing "and the user owns the activity"
      (do-it "should delete that activity"
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (model.activity/create (factory Activity))]
              (delete (:_id activity))
              (expect (nil? (model.activity/fetch-by-id (:_id activity)))))))))
    (testing "and the user does not own the activity"
      (do-it "should not delete that activity"
        (let [user1 (model.user/create (factory User))
              user2 (model.user/create (factory User))
              activity (with-user user1
                         (model.activity/create (factory Activity)))]
          (with-user user2
            (delete (:_id activity))
            (expect (model.activity/fetch-by-id (:_id activity)))))))))

(describe edit)

(describe fetch-comments)

(describe fetch-comments-remote)

(describe friends-timeline)

(describe inbox)

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

(describe like-activity)

(describe new)

(describe new-comment)

(describe show)

(describe update)

(describe user-timeline)
