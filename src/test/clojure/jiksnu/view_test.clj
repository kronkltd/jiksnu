(ns jiksnu.view-test
  (:use clj-tigase.core
        ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        ciste.views
        jiksnu.core-test
        jiksnu.file
        jiksnu.model
        jiksnu.session
        jiksnu.namespace
        jiksnu.view
        jiksnu.xmpp.element
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.file :as file]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry))

(describe parse-stream)

;; (describe parse-xml-string
;;   (do-it "should be an entry"
;;     (let [entry (slurp-classpath "entry.xml")
;;           response (parse-xml-string entry)]
;;       (expect (instance? Entry response)))))

(describe not-namespace)

(describe bare-recipient?)

(describe from-authenticated?)

(describe process-child)

;; (describe to-tigase-element
;;   (testing "a simple element"
;;     (do-it "should"
;;       (let [element
;;             {:tag :query,
;;              :attrs {:xmlns "http://onesocialweb.org/spec/1.0/vcard4#query"},
;;              :content nil}]
;;         (expect (element? (to-tigase-element element))))))
;;   (testing "a full entry" {:focus true}
;;     (do-it "should return a tigase element"
;;       (with-format :atom
;;         (with-serialization :http
;;           (let [activity (factory Activity)
;;                 element (show-section activity)
;;                 response (to-tigase-element element)]
;;             (expect (element? response))))))))

(describe add-children)

(describe add-attributes)

(describe abdera-to-tigase-element
  (do-it "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
              abdera-element (show-section activity)
              response (abdera-to-tigase-element abdera-element)]
          (expect (element? response)))))))

(describe make-minimal-item)

(describe apply-template)
