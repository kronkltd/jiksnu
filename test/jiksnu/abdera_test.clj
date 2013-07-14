(ns jiksnu.abdera-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [fseq]]
        [jiksnu.abdera :only [abdera-factory new-id get-text new-entry make-feed*]]
        [jiksnu.test-helper :only [check context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clj-time.core :as time]
            [jiksnu.namespace :as ns])
  (:import javax.xml.namespace.QName
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Feed))

(test-environment-fixture

 (context #'new-id
   (new-id) => string?)

 (context #'get-text
   (context "when the element has text content"
     (let [qname (QName. ns/atom "content")
           text (fseq :word)
           element (.newElement abdera-factory qname)]
       (.setText element text)
       (get-text element) => text))

   (context "when the element does not have any text"
     (let [qname (QName. ns/atom "content")
           element (.newElement abdera-factory qname)]
       (get-text element) => "")))

 (context #'new-entry
   (context "should return an entry"
     (new-entry) => (partial instance? Entry)))


 (context #'make-feed*
   (let [feed-map {:title "Public Activities",
                   :subtitle "All activities posted",
                   :id "http://localhost/api/statuses/public_timeline.atom",
                   :links
                   [{:href "http://localhost/", :rel "alternate", :type "text/html"}
                    {:href "http://localhost/api/statuses/public_timeline.atom",
                     :rel "self",
                     :type "application/atom+xml"}],
                   :updated (time/now),
                   }]
     (make-feed* feed-map) =>
     (check [response]
       response => (partial instance? Feed))))
 )
