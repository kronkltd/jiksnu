(ns jiksnu.actions.activity-actions-test
  (:use ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        ciste.views
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

(describe prepare-activity
  (do-it "should return an activity"
    (let [user (model.user/create (factory User))]
      (with-user user
        (let [args (factory Activity)]
          (let [response (prepare-activity args)]
            (expect (activity? response))))))))

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

(describe create
  (testing "when the user is logged in"
    (do-it "should return an activity"
      (let [user (model.user/create (factory User))]
        (with-user user
          (let [activity (factory Activity)
                response (create activity)]
            (expect (activity? response)))))))
  #_(testing "when the user is not logged in"
    (do-it "should return nil"
      (let [activity (factory Activity)
            response (create activity)]
        (expect (nil? response))))))

(describe delete
  (testing "when the activity exists"
    (testing "and the user owns the activity"
      (do-it "should delete that activity"
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (create (factory Activity {:autors [(:_id user)]}))]
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

(describe fetch-comments
  (testing "when the activity exists"
    (testing "and there are no comments"
      (do-it "should return an empty sequence"
        (let [actor (model.user/create (factory User))]
          (with-user actor
            (let [activity (create (factory Activity))
                  [_ comments] (fetch-comments activity)]
              (expect (empty? comments)))))))))

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

(describe show
  (testing "when the record exists"
    (testing "and the user is not logged in"
      (testing "and the record is public"
        (do-it "should return the activity"
          (let [author (model.user/create (factory User))
                activity (with-user author
                           (create (factory Activity)))
                response (show (:_id activity))]
            (expect (activity? response)))))
      (testing "and the record is not public"
        (do-it "should return nil"
          (let [author (model.user/create (factory User))
                activity (with-user author
                           (create (factory Activity {:public false})))
                response (show (:_id activity))]
            (expect (nil? response))))))
    (testing "and the user is logged in"
      (testing "and is the author"
        (do-it "should return the activity"
          (let [user (model.user/create (factory User))]
            (with-user user
              (let [activity (create (factory Activity))
                    response (show (:_id activity))]
                (expect (activity? response)))))))
      (testing "and is not the author"
        (testing "and is on the access list"
          (do-it "should return the activity"))
        (testing "and is not on the access list"
          (testing "and is an admin"
            (do-it "should return the activity"
              (let [user (model.user/create (factory User {:admin true}))
                    author (model.user/create (factory User))]
                (let [activity (with-user author
                                 (create (factory Activity {:public false})))]
                  (with-user user
                    (let [response (show (:_id activity))]
                      (expect (activity? response))))))))
          (testing "and is not an admin"
            (do-it "should return nil"
              (let [user (model.user/create (factory User))
                    author (model.user/create (factory User))]
                (let [activity
                      (with-user author
                        (create (factory Activity {:public false})))]
                  (with-user user
                    (let [response (show (:_id activity))]
                      (expect (nil? response)))))))))))
    (testing "and the record is not public"
      (testing "and the user is not logged in"
        (do-it "should return nil"
          (let [activity (create (factory Activity {:public false}))
                response (show (:_id activity))]
            (expect (nil? response)))))
      (testing "and the user is logged in"
        (testing "and the user is an admin"
          (do-it "should return the activity")))))
  (testing "when the record does not exist"
    (do-it "should return nil" :pending)))

(describe update)

(describe user-timeline)
