(ns jiksnu.views.activity-views-test
  (:use ciste.core
        ciste.sections
        ciste.views
        clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.core-test
        jiksnu.actions.activity-actions
        jiksnu.model
        jiksnu.session
        jiksnu.view
        jiksnu.views.activity-views)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import org.apache.abdera.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

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
