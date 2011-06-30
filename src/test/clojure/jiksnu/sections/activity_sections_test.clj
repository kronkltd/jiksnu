(ns jiksnu.sections.activity-sections-test
  (:use ciste.core
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-factory.core
        clj-tigase.core
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.activity-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; add-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest add-form "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (add-form activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest edit-form "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (edit-form activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-block "Activity :xmpp :xmpp")

(deftest index-block "Activity :html")

(deftest index-block "Activity :xml")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-block-minimal "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-block-minimal [activity])))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-line "Activity")

(deftest index-line "Activity :xmpp :xmpp")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-line-minimal "Activity :html"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-line-minimal activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest index-section "Activity :xmpp :xmpp")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest show-section "Activity :json")

(deftest show-section "Activity :xmpp :xmpp"
  (testing "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [entry (model.activity/create (factory Activity))
                  response (show-section entry)]
              (expect (not (nil? response)))
              (expect (element? response)))))))))

(deftest show-section "Activity :atom"
  (testing "should return an abdera entry"
    (with-serialization :http
      (with-format :atom
        (let [user (factory User)
              actor (model.user/create user)
              activity (factory Activity {:authors [(:_id actor)]})
              response (show-section activity)]
          (expect (instance? Entry response))
          (expect (.getId response))
          (expect (.getTitle response))
          (expect (.getUpdated response)))))))

(deftest show-section "Activity :xml")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest show-section-minimal "[Activity :html]"
  (testing "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (show-section-minimal activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; title
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest title "Activity")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Uri
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest uri "Activity"
  (testing "should be a string"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (string? (uri activity)))))))))
