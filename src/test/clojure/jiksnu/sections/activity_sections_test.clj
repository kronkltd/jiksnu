(ns jiksnu.sections.activity-sections-test
  (:use ciste.core
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-factory.core
        clojure.test
        jiksnu.core-test
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.activity-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view)
  (:require [clj-tigase.element :as element]
            [hiccup.form-helpers :as f]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry
           org.apache.abdera.ext.json.JSONUtil
           tigase.xml.Element))

(use-fixtures :each test-environment-fixture)

(deftest uri-test "Activity"
  (testing "should be a string"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (string? (uri activity)))))))))

(deftest show-section-test "Activity :atom"
  (testing "should return an abdera entry"
    (with-serialization :http
      (with-format :atom
        (let [user (factory User)
              actor (model.user/create user)
              activity (factory Activity {:authors [(:_id actor)]})
              response (show-section activity)]
          (is (instance? Entry response))
          (is (.getId response))
          (is (.getTitle response))
          (is (.getUpdated response)))))))

(deftest show-section-test "Activity :xmpp :xmpp"
  (testing "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [entry (model.activity/create (factory Activity))
                  response (show-section entry)]
              (is (not (nil? response)))
              (is (element/element? response)))))))))
