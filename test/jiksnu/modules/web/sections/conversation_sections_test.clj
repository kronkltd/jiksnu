(ns jiksnu.modules.web.sections.conversation-sections-test
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
            [net.cgrand.enlive-html :as enlive])
  (:import jiksnu.model.Conversation
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (context #'show-section
   (context "Conversation"
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :html"
           (with-format :html

             (context "when the conversation is empty"
               (let [item (Conversation.)]

                 (context "when dynamic is false"
                   (binding [ko/*dynamic* false]

                     (show-section item) =>
                     (check [response]
                       (let [resp-str (h/html response)]
                         resp-str => string?)

                       (let [doc (hiccup->doc response)]
                         (count doc) => 1
                         doc => (partial every? map?)))
                     ))
                 ))

             (context "when given a real conversation"
               (let [item (mock/a-conversation-exists)
                     activity (mock/there-is-an-activity {:conversation item})]

                 (context "when dynamic is false"
                   (binding [ko/*dynamic* false]

                     (show-section item) =>
                     (check [response]
                       (let [resp-str (h/html response)]
                         resp-str => string?)

                       (let [doc (hiccup->doc response)]
                         (count doc) => 1
                         doc => (partial every? map?)

                         (let [conv-elts (enlive/select doc [:.conversation-section])]
                           (count conv-elts) => 1

                           (doseq [elt conv-elts]
                             (get-in elt [:attrs :data-id]) => (str (:_id item))))

                         (let [conv-elts (select-by-model doc "activity")]
                           (count conv-elts) => 1)))))
                 ))
             ))
         )))
   )
 )
