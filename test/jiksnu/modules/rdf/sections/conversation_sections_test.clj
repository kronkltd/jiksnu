(ns jiksnu.modules.rdf.sections.conversation-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-serialization with-format]]
        [ciste.sections.default :only [index-block index-section
                                       show-section uri]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context hiccup->doc
                                   test-environment-fixture select-by-model]]
        jiksnu.sections.conversation-sections
        [midje.sweet :only [=>]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.rdf.sections.conversation-sections
            [net.cgrand.enlive-html :as enlive])
  (:import jiksnu.model.Conversation
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (context #'index-block
   (context "Conversation"
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :rdf"
           (with-format :rdf

             (context "when given real conversations"
               (let [n 5
                     items (doall (for [i (range n)]
                                    (mock/a-conversation-exists)))]
                 (index-block items) =>
                 (check [response]
                   response => seq?
                   response => (partial every? vector?)
                   response => (partial every? #(= (count %) 3)))))))))))

 (context #'index-section
   (context "Conversation"
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :rdf"
           (with-format :rdf

             (context "when given real conversations"
               (let [n 5
                     items (doall (for [i (range n)]
                                    (mock/a-conversation-exists)))]
                 (index-section items) =>
                 (check [response]
                   response => seq?
                   response => (partial every? vector?)
                   response => (partial every? #(= (count %) 3)))))))))))

 (context #'show-section
   (context "Conversation"
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :rdf"
           (with-format :rdf

             (context "when given a real conversation"
               (let [item (mock/a-conversation-exists)]

                 (show-section item) =>
                 (check [response]
                   response => (partial every? vector?))))
             ))
         )))
   )
 )
