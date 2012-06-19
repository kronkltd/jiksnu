(ns jiksnu.actions.stream-actions-test
  (:use midje.sweet
        jiksnu.actions.stream-actions
        jiksnu.test-helper
        jiksnu.model
        jiksnu.session
        jiksnu.actions.stream-actions
        [clj-factory.core :only [factory]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'public-timeline"
   (fact "when there are no activities"
     (fact "should be empty"
       (model.activity/drop!)
       (public-timeline) => (comp empty? :items)))
   (fact "when there are activities"
     (fact "should return a seq of activities"
       (let [author (model.user/create (factory User))]
         (with-user author
           (model.activity/create (factory Activity))))
       (public-timeline) => (every-checker
                             coll?
                             #(seq? (:items %))
                             #(= 1 (:total-records %))
                             ;; #(every? activity? (first %))
                             ))))

 (fact "#'user-timeline"
   (fact "when the user has activities"
     (model/drop-all!)
     (let [user (model.user/create (factory :local-user))
           activity (model.activity/create (factory :activity
                                                    {:author (:_id user)}))]
       (user-timeline user) =>
       (every-checker
        vector?
        (fn [response]
          (fact
            (first response) => user
            (second response) => map?
            (:total-records (second response)) => 1))))))
 
 ;; (fact "#'remote-profile"
 ;;   (remote-profile) => nil)

 ;; (fact "#'show"
 ;;   (fact "when the user exists"
 ;;     (facts "should return that user"
 ;;       (let [user (model.user/create (factory User))
 ;;             response (show user)]
 ;;         response => (partial instance? User)
 ;;         response => user))))

 ;; (fact "#'remote-user"
 ;;   (remote-user user) => user?)

 )

