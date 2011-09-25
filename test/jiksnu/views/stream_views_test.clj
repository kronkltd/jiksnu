(ns jiksnu.views.stream-views-test
  (:use (ciste core sections views)
        clj-factory.core
        clojure.test
        (jiksnu core-test model session view)
        jiksnu.actions.stream-actions
        jiksnu.views.stream-views
        midje.sweet)
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user]))
  (:import org.apache.abdera.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest apply-view-test
  (testing "#'index :atom"
    (testing "should be a map"
      (with-serialization :http
        (with-format :atom
          (with-user (model.user/create (factory User))
            (let [activity (model.activity/create (factory Activity))
                  response (apply-view {:action #'index
                                        :format :atom} [activity])]
              (is (map? response)))))))))
