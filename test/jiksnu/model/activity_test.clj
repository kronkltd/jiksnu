(ns jiksnu.model.activity-test
  (:use clj-factory.core
        clojure.test
        (jiksnu core-test model session)
        jiksnu.model.activity
        midje.sweet)
  (:require [karras.entity :as entity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

;; (deftest new-id-test
;;   (testing "should return a string"
;;     (let [response (new-id)]
;;       (is (instance? String response)))))

(deftest index-test
  (testing "when there are no activities"
    (testing "should be empty"
      (drop!)
      (let [response (index)]
        (is (empty? response)))))
  (testing "when there are activities"
    (testing "should return a seq of activities"
      (let [actor (model.user/create (factory User))]
        (drop!)
        (with-user actor
          (let [activity (create (factory Activity))
                response (index)]
            (is (seq response))
            (is (every? activity? response))))))))

(deftest fetch-by-id-test)

(deftest show-test
  (testing "when the record exists"
    (testing "and the user is not logged in"
      (testing "and the record is public"
        (testing "should return the activity"
          (let [author (model.user/create (factory User))
                activity (with-user author
                           (create (factory Activity)))
                response (show (:_id activity))]
            (is (activity? response)))))
      (testing "and the record is not public"
        (testing "should return nil"
          (let [author (model.user/create (factory User))
                activity (with-user author
                           (create (factory Activity {:public false})))
                response (show (:_id activity))]
            (is (nil? response))))))
    (testing "and the user is logged in"
      (testing "and is the author"
        (testing "should return the activity"
          (let [user (model.user/create (factory User))]
            (with-user user
              (let [activity (create (factory Activity))
                    response (show (:_id activity))]
                (is (activity? response)))))))
      (testing "and is not the author"
        (testing "and is on the access list"
          (testing "should return the activity"))
        (testing "and is not on the access list"
          (testing "and is an admin"
            (testing "should return the activity"
              (let [user (model.user/create (factory User {:admin true}))
                    author (model.user/create (factory User))]
                (let [activity (with-user author
                                 (create (factory Activity {:public false})))]
                  (with-user user
                    (let [response (show (:_id activity))]
                      (is (activity? response))))))))
          (testing "and is not an admin"
            (testing "should return nil"
              (let [user (model.user/create (factory User))
                    author (model.user/create (factory User))]
                (let [activity
                      (with-user author
                        (create (factory Activity {:public false})))]
                  (with-user user
                    (let [response (show (:_id activity))]
                      (is (nil? response)))))))))))
    (testing "and the record is not public"
      (testing "and the user is not logged in"
        (testing "should return nil"
          (let [activity (create (factory Activity {:public false}))
                response (show (:_id activity))]
            (is (nil? response)))))
      (testing "and the user is logged in"
        (testing "and the user is an admin"
          (testing "should return the activity")))))
  (testing "when the record does not exist"
    (testing "should return nil" :pending)))

(deftest drop!-test
  (testing "when there are activities"
    (testing "should delete all of them"
      (create (factory Activity))
      (drop!)
      (is (empty? (index))))))

(deftest delete-test
  (testing "when a user is logged in"
    (testing "and is the owner of the activity"
      (testing "should delete the activity"
        (let [actor (model.user/create (factory User))]
          (with-user actor
            (let [activity (create (factory Activity))]
              (delete activity)
              (is (nil? (show (:_id activity)))))))))))

(deftest find-by-user-test)

(deftest add-comment-test)

(deftest set-id-test
  (testing "when there is an id"
    (testing "should not change the value"
      (let [activity (factory Activity)
            response (set-id activity)]
        (is (= (:_id activity)
                   (:_id response))))))
  (testing "when there is no id"
    (testing "should add an id key"
      (let [activity (factory Activity)
            response (set-id activity)]
        (:_id response)))))

(deftest set-updated-time-test
  (testing "when there is an updated property"
    (testing "should not change the value"
      (let [activity (factory Activity)
            response (set-updated-time activity)]
        (is (= (:updated activity)
                   (:updated response))))))
  (testing "when there is no updated property"
    (testing "should add an updated property"
      (let [activity (dissoc (factory Activity) :updated)
            response (set-updated-time activity)]
        (is (:updated response))))))

(deftest test-set-remote
  (testing "when the local flag is not set"
    (fact "the local flag should be false"
      (let [activity (factory Activity)]
        (set-remote activity) => (contains {:local false}))))
  (testing "when the local flag is set to true"
    (fact "the local flag should be true"
      (let [activity (factory Activity {:local true})]
        (set-remote activity) => (contains {:local true}))))
  (testing "when the local flag is set to false"
    (fact "the local flag should be false"
      (let [activity (factory Activity {:local false})]
        (set-remote activity) => (contains {:local false})))))

