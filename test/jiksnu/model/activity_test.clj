(ns jiksnu.model.activity-test
  (:use clj-factory.core
        clojure.test
        (jiksnu test-helper model session)
        jiksnu.model.activity
        midje.sweet)
  (:require [karras.entity :as entity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture)

;; (deftest index-test)

(fact "when there are no activities"
  (fact "should be empty"
    (drop!)
    (let [response (index)]
      (is (empty? response)))))
(fact "when there are activities"
  (fact "should return a seq of activities"
    (let [actor (model.user/create (factory User))]
      (drop!)
      (with-user actor
        (let [activity (create (factory Activity))
              response (index)]
          response => truthy
          (is (every? activity? response)))))))

;; (deftest fetch-by-id-test)

;; (deftest show-test)

(fact "when the record exists"
  (fact "and the user is not logged in"
    (fact "and the record is public"
      (fact "should return the activity"
        (let [author (model.user/create (factory User))
              activity (with-user author
                         (create (factory Activity)))
              response (show (:_id activity))]
          response => activity?)))
    
    (fact "and the record is not public"
      (fact "should return nil"
        (let [author (model.user/create (factory User))
              activity (with-user author
                         (create (factory Activity {:public false})))
              response (show (:_id activity))]
          response => nil?))))
  
  (fact "and the user is logged in"
    (fact "and is the author"
      (fact "should return the activity"
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (create (factory Activity))
                  response (show (:_id activity))]
              (is (activity? response)))))))
    
    (fact "and is not the author"
      (fact "and is on the access list"
        (fact "should return the activity"))
      
      (fact "and is not on the access list"
        (fact "and is an admin"
          (fact "should return the activity"
            (let [user (model.user/create (factory User {:admin true}))
                  author (model.user/create (factory User))]
              (let [activity (with-user author
                               (create (factory Activity {:public false})))]
                (with-user user
                  (let [response (show (:_id activity))]
                    (is (activity? response))))))))
        
        (fact "and is not an admin"
          (fact "should return nil"
            (let [user (model.user/create (factory User))
                  author (model.user/create (factory User))]
              (let [activity
                    (with-user author
                      (create (factory Activity {:public false})))]
                (with-user user
                  (let [response (show (:_id activity))]
                    response => nil?)))))))))
  
  (fact "and the record is not public"
    (fact "and the user is not logged in"
      (fact "should return nil"
        (let [activity (create (factory Activity {:public false}))
              response (show (:_id activity))]
          response => nil?)))
    
    (fact "and the user is logged in"
      (fact "and the user is an admin"
        (fact "should return the activity")))))

(fact "when the record does not exist"
  (fact "should return nil" :pending))

;; (deftest drop!-test)

(fact "when there are activities"
  (fact "should delete all of them"
    (create (factory Activity))
    (drop!)
    (index) => empty?))

;; (deftest delete-test)

(fact "when a user is logged in"
  (fact "and is the owner of the activity"
    (fact "should delete the activity"
      (let [actor (model.user/create (factory User))]
        (with-user actor
          (let [activity (create (factory Activity))]
            (delete activity)
            (show (:_id activity)) => nil?))))))

;; (deftest find-by-user-test)

;; (deftest add-comment-test)

;; (deftest set-id-test)

(fact "when there is an id"
  (fact "should not change the value"
    (let [activity (factory Activity)
          response (set-id activity)]
      (is (= (:_id activity)
             (:_id response))))))

(fact "when there is no id"
  (fact "should add an id key"
    (let [activity (factory Activity)
          response (set-id activity)]
      (:_id response))))

;; (deftest set-updated-time-test)

(fact "when there is an updated property"
  (fact "should not change the value"
    (let [activity (factory Activity)
          response (set-updated-time activity)]
      (:updated response) => (:updated activity))))

(fact "when there is no updated property"
  (fact "should add an updated property"
    (let [activity (dissoc (factory Activity) :updated)
          response (set-updated-time activity)]
      (:updated response) => truthy)))

;; (deftest test-set-remote)

(fact "when the local flag is not set"
  (fact "the local flag should be false"
    (let [activity (factory Activity)]
      (set-remote activity) => (contains {:local false}))))

(fact "when the local flag is set to true"
  (fact "the local flag should be true"
    (let [activity (factory Activity {:local true})]
      (set-remote activity) => (contains {:local true}))))

(fact "when the local flag is set to false"
  (fact "the local flag should be false"
    (let [activity (factory Activity {:local false})]
      (set-remote activity) => (contains {:local false}))))

