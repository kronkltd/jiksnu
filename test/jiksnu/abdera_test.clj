(ns jiksnu.abdera-test
  (:use (ciste [debug :only (spy)])
        (clj-factory [core :only (fseq)])
        clojure.test
        (jiksnu abdera core-test)
        midje.sweet)
  (:require (jiksnu [namespace :as namespace]))
  (:import javax.xml.namespace.QName
           ;; org.apache.abdera.model.Element
           org.apache.abdera2.model.Entry))

(deftest test-new-id
  (fact "should return a string"
    (new-id) => string?))

(deftest test-get-text
  (testing "when the element has text content"
    (fact "should return that string"
      (let [qname (QName. namespace/atom "content")
            text (fseq :word)
            element (.newElement *abdera-factory* qname)]
        (.setText element text)
        (get-text element) => text)))
  (testing "when the element does not have any text"
    (fact "should return an empty string"
      (let [qname (QName. namespace/atom "content")
            element (.newElement *abdera-factory* qname)]
        (get-text element) => ""))))

(deftest test-new-entry
  (fact "should return an entry"
    (new-entry) => (partial instance? Entry)))
