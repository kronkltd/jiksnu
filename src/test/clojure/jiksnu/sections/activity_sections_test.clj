(ns jiksnu.sections.activity-sections-test
  (:use ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-tigase.core
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.activity-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
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

(describe add-form "Activity :html"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (add-form activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit-form
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe edit-form "Activity :html"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (edit-form activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe index-block "Activity :xmpp :xmpp")

(describe index-block "Activity :html")

(describe index-block "Activity :xml")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-block-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe index-block-minimal "Activity :html"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-block-minimal [activity])))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe index-line "Activity")

(describe index-line "Activity :xmpp :xmpp")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-line-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe index-line-minimal "Activity :html"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-line-minimal activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe index-section "Activity :xmpp :xmpp")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe show-section "Activity :json")

(describe show-section "Activity :xmpp :xmpp"
  (do-it "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [entry (model.activity/create (factory Activity))
                  response (show-section entry)]
              (expect (not (nil? response)))
              (expect (element? response)))))))))

(describe show-section "Activity :atom"
  (do-it "should return an abdera entry"
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

(describe show-section "Activity :xml")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section-minimal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe show-section-minimal "[Activity :html]"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (show-section-minimal activity)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; title
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe title "Activity")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Uri
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(describe uri "Activity"
  (do-it "should be a string"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (string? (uri activity)))))))))
