(ns jiksnu.sections.activity-sections-test
  (:use (ciste [config :only [with-environment]]
               core sections views)
        ciste.sections.default
        (clj-factory [core :only [factory]])
        clojure.test
        (jiksnu test-helper model session)
        jiksnu.sections.activity-sections
        midje.sweet)
  (:require (clj-tigase [element :as element])
            [hiccup.form-helpers :as f]
            (jiksnu [model :as model]
                    [namespace :as namespace])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.xmpp [element :as xmpp.element]))
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.model.Entry
           tigase.xml.Element))

(with-environment :test
  (test-environment-fixture)

  (fact "uri Activity"
    (facts "should be a string"
      (uri activity) => string?
      (against-background
        (around
         :facts
         (with-serialization :http
           (with-format :html
             (with-user (model.user/create (factory User))
               (let [activity (model.activity/create (factory Activity))]
                 ?form))))))))

  (fact "show-section Activity :atom"
    (facts "should return an abdera entry"
      (let [response (show-section activity)]
        (instance? Entry response) => truthy
        (.getId response) => truthy
        (.getTitle response) => truthy
        (.getUpdated response) => truthy)
      (against-background
        (around
         :facts
         (with-serialization :http
           (with-format :atom
             (let [author-map {:authors
                               [(:_id (model.user/create (factory User)))]}
                   activity (factory Activity author-map)]
               ?form)))))))

  (fact "show-section Activity :xmpp :xmpp"
    (facts "should return an element"
      (show-section entry) => element/element?
      (against-background
        (around
         :facts
         (with-serialization :xmpp
           (with-format :xmpp
             (with-user (model.user/create (factory User))
               (let [entry (model.activity/create (factory Activity))]
                 ?form)))))))))
