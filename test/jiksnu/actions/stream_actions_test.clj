(ns jiksnu.actions.stream-actions-test
  (:use midje.sweet
        jiksnu.actions.stream-actions
        jiksnu.test-helper
        jiksnu.model
        jiksnu.session
        jiksnu.actions.stream-actions
        (clj-factory [core :only [factory]]))
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

;; (test-environment-fixture)

;; (deftest index-test)

;; (fact "when there are no activities"
;;   (fact "should be empty"
;;     (model.activity/drop!)
;;     (index) => empty?))
;; (fact "when there are activities"
;;   (fact "should return a seq of activities"
;;     (let [author (model.user/create (factory User))]
;;       (with-user author
;;         (model.activity/create (factory Activity))))
;;     (let [response (index)]
;;       response => seq?
;;       response => (partial every? activity?))))

;; (deftest test-remote-profile)

;; (fact
;;   (remote-profile) => nil)

;; (deftest test-show)

;; (fact "when the user exists"
;;   (facts "should return that user"
;;          (let [user (model.user/create (factory User))
;;                response (show user)]
;;            response => (partial instance? User)
;;            response => user)))

;; (deftest test-remote-user)

;; (fact
;;   (remote-user user) => user?)

