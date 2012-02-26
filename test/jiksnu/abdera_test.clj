(ns jiksnu.abdera-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]])
        (clj-factory [core :only [fseq]])
        clojure.test
        (jiksnu abdera test-helper)
        midje.sweet)
  (:require (jiksnu [namespace :as namespace]))
  (:import javax.xml.namespace.QName
           ;; org.apache.abdera.model.Element
           org.apache.abdera2.model.Entry))

(test-environment-fixture

 (fact "new-id"
   (fact "should return a string"
     (new-id) => string?))

 (fact "get-text"
   (fact "when the element has text content"
     (fact "should return that string"
       (let [qname (QName. namespace/atom "content")
             text (fseq :word)
             element (.newElement *abdera-factory* qname)]
         (.setText element text)
         (get-text element) => text)))

   (fact "when the element does not have any text"
     (fact "should return an empty string"
       (let [qname (QName. namespace/atom "content")
             element (.newElement *abdera-factory* qname)]
         (get-text element) => ""))))

 (fact "new-entry"
   (fact "should return an entry"
     (new-entry) => (partial instance? Entry))))
