(ns jiksnu.sections.activity-sections-test
  (:use (ciste core sections views)
        ciste.sections.default
        clj-factory.core
        clojure.test
        (jiksnu core-test model namespace session)
        jiksnu.helpers.activity-helpers
        jiksnu.sections.activity-sections
        jiksnu.xmpp.element
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [hiccup.form-helpers :as f]
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user]))
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry
           org.apache.abdera.ext.json.JSONUtil
           tigase.xml.Element))

(use-fixtures :each test-environment-fixture)

(deftest test-uri "Activity"
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

(deftest test-show-section "Activity :atom"
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

(deftest test-show-section "Activity :xmpp :xmpp"
  (facts "should return an element"
    (show-section entry) => element/element?
    (against-background
      (around
       :facts
       (with-serialization :xmpp
         (with-format :xmpp
           (with-user (model.user/create (factory User))
             (let [entry (model.activity/create (factory Activity))]
               ?form))))))))
