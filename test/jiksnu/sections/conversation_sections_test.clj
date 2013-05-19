(ns jiksnu.sections.conversation-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-serialization with-format]]
        [ciste.sections.default :only [index-block index-section
                                       show-section uri]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
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
            [jiksnu.model.user :as model.user])
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

             (fact "when dynamic is false"
               (binding [ko/*dynamic* false]
                 (let [item (Conversation.)]
                   (show-section item) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)))))))
           ))
       ))
   )
 )
