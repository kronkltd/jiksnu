(ns jiksnu.modules.atom.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [index-block index-section
                                       show-section uri]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.session
        jiksnu.sections.activity-sections
        [midje.sweet :only [=>]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (context #'show-section
   (context "Activity"
     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :atom"
           (with-format :atom

             (context "when given a real activity"
               (let [activity (mock/there-is-an-activity)]
                 (show-section activity) =>
                 (check [response]
                   response => (partial instance? Entry)
                   (.getId response) => (partial instance? IRI)
                   ;; (.getUpdated response) => (partial instance? org.apache.abdera.model.DateTime)
                   (.getTitle response) => string?
                   (.getAuthor response) => (partial instance? Person))))

             (context "when given a partial activity"
               (let [activity (factory :activity)]
                 (show-section activity) =>
                 (check [response]
                   response => (partial instance? Entry))))
            ))
         ))))
 )
