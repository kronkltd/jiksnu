(ns jiksnu.xmpp.element-test
  (:use ciste.core
        ciste.sections
        ciste.sections.default
        clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.core-test
        jiksnu.model
        jiksnu.session
        jiksnu.namespace
        jiksnu.view
        jiksnu.xmpp.element)
  (:import jiksnu.model.Activity))

(deftest abdera-to-tigase-element-test
  (testing "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
              abdera-element (show-section activity)
              response (abdera-to-tigase-element abdera-element)]
          (is (element? response)))))))
