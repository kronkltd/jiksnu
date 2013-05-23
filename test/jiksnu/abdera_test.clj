(ns jiksnu.abdera-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [fseq]]
        [jiksnu.abdera :only [abdera-factory new-id get-text new-entry]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> every-checker fact]])
  (:require [jiksnu.namespace :as ns])
  (:import javax.xml.namespace.QName
           org.apache.abdera.model.Entry))

(test-environment-fixture

 (fact "new-id"
   (fact "should return a string"
     (new-id) => string?))

 (fact "get-text"
   (fact "when the element has text content"
     (fact "should return that string"
       (let [qname (QName. ns/atom "content")
             text (fseq :word)
             element (.newElement abdera-factory qname)]
         (.setText element text)
         (get-text element) => text)))

   (fact "when the element does not have any text"
     (fact "should return an empty string"
       (let [qname (QName. ns/atom "content")
             element (.newElement abdera-factory qname)]
         (get-text element) => ""))))

 (fact "new-entry"
   (fact "should return an entry"
     (new-entry) => (partial instance? Entry)))

 )
