(ns jiksnu.xmpp.element-test
  (:use ciste.core
        ciste.debug
        ciste.sections
        ciste.sections.default
        clj-factory.core
        clojure.test
        jiksnu.core-test
        jiksnu.model
        jiksnu.session
        jiksnu.namespace
        jiksnu.view
        jiksnu.xmpp.element)
  (:require [clj-tigase.element :as element])
  (:import jiksnu.model.Activity))

(use-fixtures :each test-environment-fixture)

(deftest abdera-to-tigase-element-test
  (testing "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
              abdera-element (show-section activity)
              response (abdera-to-tigase-element (spy abdera-element))]
          (is (element/element? response)))))))
