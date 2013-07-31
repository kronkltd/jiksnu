(ns jiksnu.modules.rdf.sections.activity-sections-test
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
            [jiksnu.model.user :as model.user]
            jiksnu.modules.rdf.sections.activity-sections)
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (context #'index-block
   (context "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [activity (mock/there-is-an-activity)]
         (index-block [activity]) =>
         (check [response]
           response => (partial every? (fn [t]
                                         (and (vector? t)
                                              (= 3 (count t))))))))))

 (context #'index-section
   (context "Activity"
     (context "when the context is [:http :rdf]"
       (with-context [:http :rdf]
         (let [activity (mock/there-is-an-activity)]
           (index-section [activity]) =>
           (check [r]
             (doseq [t r]
               t => vector?
               (count t) => 3)))))))

 )
