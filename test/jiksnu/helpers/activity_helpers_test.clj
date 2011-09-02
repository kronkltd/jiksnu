(ns jiksnu.helpers.activity-helpers-test
  (:use (ciste core
               [debug :only (spy)]
               sections)
        ciste.sections.default
        clj-factory.core
        clj-tigase.core
        clojure.test
        (jiksnu core-test model namespace session view)
        jiksnu.helpers.activity-helpers
        [karras.entity :only (make)]
        midje.sweet)
  (:require jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(background
 (around :facts
   (with-environment :test
     (let [actor (factory User)]
       ?form))))

;; (deftest to-json-test
;;   (testing "should not be nil"
;;     (with-serialization :http
;;       (with-format :atom
;;         (let [activity (factory Activity)
;;               entry (show-section activity)
;;               response (to-json entry)]
;;           (is (not (nil? response))))))))

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
