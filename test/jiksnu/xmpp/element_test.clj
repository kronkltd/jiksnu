(ns jiksnu.xmpp.element-test
  (:use (ciste core debug sections)
        ciste.sections.default
        [clj-factory.core :only (factory)]
        [clojure.test :only (deftest is use-fixtures testing)]
        (jiksnu core-test model session namespace view)
        jiksnu.xmpp.element
        [karras.entity :only (make)])
  (:require [clj-tigase.element :as element]
            jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity))

(use-fixtures :each test-environment-fixture)

(deftest abdera-to-tigase-element-test
  (testing "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        ;; TODO: fix clj-factory
        (let [activity (make Activity (factory Activity))
              abdera-element (show-section activity)
              response (abdera-to-tigase-element abdera-element)]
          (is (element/element? response)))))))
