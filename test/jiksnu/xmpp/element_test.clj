(ns jiksnu.xmpp.element-test
  (:use (ciste core debug sections)
        ciste.sections.default
        [clj-factory.core :only (factory)]
        [clojure.test :only (deftest is use-fixtures testing)]
        (jiksnu core-test model session view)
        jiksnu.xmpp.element
        [karras.entity :only (make)])
  (:require [clj-tigase.element :as element]
            (jiksnu [namespace :as namespace])
            jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity))

(use-fixtures :once test-environment-fixture)

(deftest abdera-to-tigase-element-test
  (testing "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
              abdera-element (show-section activity)
              response (abdera-to-tigase-element abdera-element)]
          (is (element/element? response)))))))
