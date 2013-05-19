(ns jiksnu.sections.conversation-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-serialization with-format]]
        [ciste.sections.default :only [index-block index-section
                                       show-section uri]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [hiccup->doc test-environment-fixture]]
        jiksnu.sections.conversation-sections
        [midje.sweet :only [fact future-fact => every-checker]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [net.cgrand.enlive-html :as enlive])
  (:import jiksnu.model.Conversation
           org.apache.abdera2.common.iri.IRI
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (fact "#'show-section Conversation"
   (fact "when the serialization is :http"
     (with-serialization :http

       (fact "when the format is :html"
         (with-format :html

           (fact "when the conversation is empty"
             (let [item (Conversation.)]

               (fact "when dynamic is false"
                 (binding [ko/*dynamic* false]

                   (show-section item) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)

                       (let [doc (hiccup->doc response)]
                         (count doc) => 1
                         doc => (partial every? map?))))
                   ))
               ))

           (fact "when given a real conversation"
             (let [item (mock/a-conversation-exists)]

               (fact "when dynamic is false"
                 (binding [ko/*dynamic* false]

                   (show-section item) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)

                       (let [doc (hiccup->doc response)]
                         (count doc) => 1
                         doc => (partial every? map?)

                         (let [conv-elts (->> [(enlive/attr= :data-model "conversation")]
                                              (enlive/select doc))]
                           (count conv-elts) => 1

                           (doseq [elt conv-elts]
                             (get-in elt [:attrs :data-id]) => (str (:_id item)))))))
                   ))
               ))
           ))
       ))
   )
 )
