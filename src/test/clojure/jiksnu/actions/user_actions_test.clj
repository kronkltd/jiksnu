(ns jiksnu.actions.user-actions-test
  (:use clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.model
        jiksnu.session
        jiksnu.actions.user-actions
        jiksnu.view)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(deftest show-test
  (testing "when the user exists"
    (testing "should return that user"
      (model.user/drop!)
      (let [user (model.user/create (factory User))
            response (show user)]
        (expect (instance? User response))
        (expect (= response user))))))
