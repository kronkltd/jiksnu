(ns jiksnu.modules.atom.sections.activity-sections-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.mock :as mock])
  (:import org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Person))

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
