(ns jiksnu.model.activity-test
  (:use [lazytest.describe :only (describe it testing given do-it)]
        jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.model.activity
        jiksnu.session
        [lazytest.expect :only (expect)])
  (:require [karras.entity :as entity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe new-id
  (it "should return a string"
    (let [response (new-id)]
      (instance? String response))))

(describe set-id
  (given [activity (factory Activity)]
    (testing "when there is an id"
      (it "should not change the value"
        (let [response (set-id activity)]
          (= (:_id activity)
             (:_id response)))))
    (testing "when there is no id"
      (it "should add an id key"
        (let [response (set-id activity)]
          (:_id response))))))

(describe set-updated-time
  (testing "when there is an updated property"
    (given [activity (factory Activity)]
      (it "should not change the value"
        (let [response (set-updated-time activity)]
          (= (:updated activity)
             (:updated response))))))
  (testing "when there is no updated property"
    (given [activity (dissoc (factory Activity) :updated)]
      (it "should add an updated property"
        (let [response (set-updated-time activity)]
          (:updated response))))))

(describe prepare-activity)

(describe create
  (testing "when the user is logged in"
    (do-it "should return an activity"
      (with-environment :test
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (factory Activity)
                  response (create activity)]
              (expect (activity? response))))))))
  (testing "when the user is not logged in"
    (do-it "should return nil"
      (with-environment :test
        (let [activity (factory Activity)
              response (create activity)]
          (expect (nil? response)))))))

(describe index
  (testing "when there are no activities"
    (do-it "should be empty"
      (with-environment :test
        (drop!)
        (let [response (index)]
          (expect (empty? response))))))
  (testing "when there are activities"
    (do-it "should return a seq of activities"
      (with-environment :test
        (let [actor (model.user/create (factory User))]
          (drop!)
          (with-user actor
            (let [activity (create (factory Activity))
                  response (index)]
             (expect (seq response))
             (expect (every? activity? response)))))))))

(describe show
  (testing "when the record exists"
    (testing "and the user is not logged in"
      (testing "and the record is public"
        (do-it "should return the activity"
          (with-environment :test
            (let [author (model.user/create (factory User))
                  activity (with-user author
                             (create (factory Activity)))
                  response (show (:_id activity))]
              (expect (activity? response))))))
      (testing "and the record is not public"
        (do-it "should return nil"
          (with-environment :test
            (let [author (model.user/create (factory User))
                  activity (with-user author
                             (create (factory Activity {:public false})))
                  response (show (:_id activity))]
              (expect (nil? response)))))))
    (testing "and the user is logged in"
      (testing "and is the author"
        (do-it "should return the activity"
          (with-environment :test
            (let [user (model.user/create (factory User))]
              (with-user user
                (let [activity (create (factory Activity))
                      response (show (:_id activity))]
                  (expect (activity? response))))))))
      (testing "and is not the author"
        (testing "and is on the access list"
          (do-it "should return the activity"))
        (testing "and is not on the access list"
          (testing "and is an admin"
            (do-it "should return the activity"
              (with-environment :test
                (let [user (model.user/create (factory User {:admin true}))
                      author (model.user/create (factory User))]
                  (let [activity (with-user author
                                   (create (factory Activity {:public false})))]
                    (with-user user
                      (let [response (show (:_id activity))]
                        (expect (activity? response)))))))))
          (testing "and is not an admin"
            (do-it "should return nil"
              (with-environment :test
                (let [user (model.user/create (factory User))
                      author (model.user/create (factory User))]
                  (let [activity
                        (with-user author
                          (create (factory Activity {:public false})))]
                    (with-user (:_id user)
                      (let [response (show (:_id activity))]
                        (expect (nil? response))))))))))))
    (testing "and the record is not public"
      (testing "and the user is not logged in"
        (do-it "should return nil"
          (with-environment :test
            (let [activity (create (factory Activity {:public false}))
                  response (show (:_id activity))]
              (expect (nil? response))))))
      (testing "and the user is logged in"
        (testing "and the user is an admin"
          (do-it "should return the activity")))))
  (testing "when the record does not exist"
    (it "should return nil" :pending)))

(describe drop!
  (testing "when there are activities"
    (it "should delete all of them"
      (with-environment :test
        (create (factory Activity))
        (drop!)
        (expect (empty? (index)))))))

(describe delete
  (testing "when a user is logged in"
    (testing "and is the owner of the activity"
      (do-it "should delete the activity"
        (with-environment :test
          (let [actor (model.user/create (factory User))]
            (with-user actor
              (let [activity (create (factory Activity))]
                (delete (:_id activity))
                (expect (nil? (show (:_id activity))))))))))))
