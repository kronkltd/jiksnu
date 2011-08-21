(ns jiksnu.actions.user-actions-test
  (:use [clj-factory.core :only (factory)]
        clojure.test
        (jiksnu core-test model)
        jiksnu.actions.user-actions
        midje.sweet)
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(background
 (around :facts (with-environment :test
                  (model.user/drop!)
                  ?form)))

(deftest show-test
  (testing "when the user exists"
    (facts "should return that user"
      (let [user (model.user/create (factory User))
            response (show user)]
        response => (partial instance? User)
        response => user))))
