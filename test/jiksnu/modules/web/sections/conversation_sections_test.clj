(ns jiksnu.modules.web.sections.conversation-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-serialization with-format]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context hiccup->doc
                                   test-environment-fixture select-by-model]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [net.cgrand.enlive-html :as enlive])
  (:import jiksnu.model.Conversation
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Person
           org.joda.time.DateTime))

(test-environment-fixture

 (context #'show-section
   (context "Conversation"
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :html"
           (with-format :html

             (context "when the conversation is empty"
               (let [item (Conversation.)]

                 (show-section item) =>
                 (check [response]
                        (let [resp-str (h/html response)]
                          resp-str => string?)

                        (let [doc (hiccup->doc response)]
                          (count doc) => 1
                          doc => (partial every? map?)))))

             (context "when given a real conversation"
               (let [item (mock/a-conversation-exists)
                     activity (mock/there-is-an-activity {:conversation item})]

                 (show-section item) =>
                 (check [response]
                        (let [resp-str (h/html response)]
                          resp-str => string?)

                        (let [doc (hiccup->doc response)]
                          (count doc) => 1
                          doc => (partial every? map?)

                          (let [conv-elts (enlive/select doc [:.conversation-section])]
                            (count conv-elts) => 1)))))
             ))
         )))
   )
 )
