(ns jiksnu.view-test
  (:use clj-tigase.core
        ciste.factory
        ciste.sections
        ciste.view
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

(describe node-value)

(describe pubsub-element?)

(describe packet?)

(describe iq-elements)

(describe pubsub-items
  (do-it "should return a seq of elements"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (model.activity/create (factory Activity))
                  element (make-element (index-section [activity]))
                  packet (make-packet
                          {:to (make-jid user)
                           :from (make-jid user)
                           :type :set
                           :body element})
                  response (pubsub-items packet)]
              (expect (every? element? response)))))))))

(describe bare-recipient?)

(describe from-authenticated?)

(describe get-items)

(describe make-request
  (testing "a pubsub publish"
    (do-it "should return a map"
      (let [user (model.user/create (factory User))
            packet (make-packet
                    {:to (make-jid user)
                     :from (make-jid user)
                     :type :get
                     :id (fseq :id)})
            response (make-request packet)]
        (expect (map? response))))))

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

(describe get-qname)

(describe make-element-qname)

(describe abdera-to-tigase-element
  (do-it "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)
              abdera-element (show-section activity)
              response (abdera-to-tigase-element abdera-element)]
          (expect (element? response)))))))

(describe respond-with)

(describe make-minimal-item)

(describe apply-template)

(describe make-jid)

(describe deliver-packet!)
