(ns jiksnu.model.activity-test
  (:use ciste.factory
        jiksnu.model
        jiksnu.model.activity
        jiksnu.session
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [karras.entity :as entity]
            [jiksnu.model.user :as model.user]
            jiksnu.core-test)
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe new-id
  (do-it "should return a string"
    (let [response (new-id)]
      (expect (instance? String response)))))

(describe index
  (testing "when there are no activities"
    (do-it "should be empty"
      (drop!)
      (let [response (index)]
        (expect (empty? response)))))
  (testing "when there are activities"
    (do-it "should return a seq of activities"
      (let [actor (model.user/create (factory User))]
        (drop!)
        (with-user actor
          (let [activity (create (factory Activity))
                response (index)]
            (expect (seq response))
            (expect (every? activity? response))))))))

(describe fetch-by-id)

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

(describe drop!
  (testing "when there are activities"
    (do-it "should delete all of them"
      (create (factory Activity))
      (drop!)
      (expect (empty? (index))))))

(describe delete
  (testing "when a user is logged in"
    (testing "and is the owner of the activity"
      (do-it "should delete the activity"
        (let [actor (model.user/create (factory User))]
          (with-user actor
            (let [activity (create (factory Activity))]
              (delete activity)
              (expect (nil? (show (:_id activity)))))))))))

(describe find-by-user)

(describe add-comment)

