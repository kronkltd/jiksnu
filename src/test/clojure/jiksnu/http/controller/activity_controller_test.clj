(ns jiksnu.http.controller.activity-controller-test
  (:use jiksnu.factory
        jiksnu.http.controller.activity-controller
        jiksnu.model
        jiksnu.session
        [lazytest.expect :only (expect)]
        [lazytest.describe :only (describe testing do-it)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe index
  (testing "when there are no activities"
    (do-it "should be empty"
      (model.activity/drop!)
      (let [request {}
            response (index request)]
        (expect (empty? response)))))
  (testing "when there are activities"
    (do-it "should return a seq of activities"
      (let [author (model.user/create (factory User))]
        (with-user author
          (model.activity/create (factory Activity))))
      (let [request {}
            response (index request)]
        (expect (seq response))
        (expect (every? activity? response))))))

(describe create)

(describe new)

(describe show)

(describe update)

(describe delete)

(describe edit)
