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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; add-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest add-form-test "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (vector? (add-form activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest edit-form-test "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (vector? (edit-form activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-block-test "Activity :xmpp :xmpp")

(deftest index-block-test "Activity :html")

(deftest index-block-test "Activity :xml")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-block-minimal-test "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (vector? (index-block-minimal [activity])))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-line-test "Activity")

(deftest index-line-test "Activity :xmpp :xmpp")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-line-minimal-test "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (vector? (index-line-minimal activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-section-test "Activity :xmpp :xmpp")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest show-section-test "Activity :json")

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

(deftest show-section-test "Activity :xml")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest show-section-minimal-test "[Activity :html]"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (vector? (show-section-minimal activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; title
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest title-test "Activity")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Uri
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest uri-test "Activity"
  (testing "should be a string"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (is (string? (uri activity)))))))))
