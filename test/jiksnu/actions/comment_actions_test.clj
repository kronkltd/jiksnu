(ns jiksnu.actions.comment-actions-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        midje.sweet
        (jiksnu core-test
                [session :only (with-user)])
        jiksnu.actions.comment-actions)
  (:require (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [user :as model.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest new-comment-test)

(deftest fetch-comments-test
  (testing "when the activity exists"
    (testing "and there are no comments"
      (fact "should return an empty sequence"
        (let [actor (model.user/create (factory User))]
          (with-user actor
            (let [activity (actions.activity/create (factory Activity))
                  [_ comments] (fetch-comments activity)]
              comments => empty?)))))))

(deftest fetch-comments-remote-test)


