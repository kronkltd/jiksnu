(ns jiksnu.helpers.activity-helpers-test
  (:use (ciste core
               [debug :only (spy)]
               sections)
        ciste.sections.default
        clj-factory.core
        clj-tigase.core
        clojure.test
        (jiksnu test-helper model session view)
        jiksnu.helpers.activity-helpers
        [karras.entity :only (make)]
        midje.sweet)
  (:require (jiksnu [namespace :as namespace])
            jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture)

;; (background
;;  (around :facts
;;    (let [actor (factory User)]
;;      ?form)))

;; (deftest to-json-test)

;; (fact "should not be nil"
;;   (with-serialization :http
;;     (with-format :atom
;;       (let [activity (factory Activity)
;;             entry (show-section activity)
;;             response (to-json entry)]
;;         (is (not (nil? response)))))))

