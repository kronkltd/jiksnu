(ns jiksnu.xmpp.view-test
  (:use ciste.view
        clojure.contrib.pprint
        jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.xmpp.view
        jiksnu.view
        [lazytest.describe :only (describe it do-it testing given)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.file :as file]
            [jiksnu.model.activity :as activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe node-value)

(describe children)

(describe ns-prefix
  (testing "when the key name is empty"
    (given [k ""]
      (do-it "should have just the xmlns"
        (let [response (ns-prefix k)]
          (expect          (= response "xmlns")))))))

(describe element?)

(describe pubsub-element?)

(describe packet?)

(describe iq-elements)

(describe pubsub-items
  (it "should return a seq of elements"
    (every? element? (pubsub-items (mock-activity-publish-request-packet)))))

(describe bare-recipient?)

(describe from-authenticated?)

(describe get-items)

(describe make-request
  (testing "a pubsub publish"
    (do-it "should return a map"
      (let [packet (mock-activity-publish-request-packet)
            response (make-request packet)]
        (expect (map? response))))))

(describe process-child)

(describe to-tigase-element
  (testing "a simple element"
    (given [element
            {:tag :query,
             :attrs {:xmlns "http://onesocialweb.org/spec/1.0/vcard4#query"},
             :content nil}]
      (do-it "should"
        (expect (element? (to-tigase-element element))))))
  (testing "a full entry"
    (given [element (file/read-xml "entry.xml")]
     (do-it "should return a tigase element"
       (let [response (to-tigase-element element)]
         (expect         (element? response)))))))

(describe assign-namespace)

(describe element-name)

(describe add-children)

(describe add-attributes)

(describe parse-qname)

(describe merge-namespaces)

(describe get-qname)

(describe make-element-qname)

(describe abdera-to-tigase-element
  (do-it "should return a tigase element"
    (with-serialization :xmpp
      (with-format :atom
        (let [activity (factory Activity)]
         (let [abdera-element (atom.view.activity/to-entry activity)]
           (let [response (abdera-to-tigase-element abdera-element)]
             (expect  (element? response)))))))))

(describe make-element)

(describe respond-with)

(describe make-minimal-item)

#_(describe make-item
  (given [entry (factory Activity)]
    (given [response (make-item entry)]
      (do-it "should return a tigase element"
        (expect (element? response))))))

(describe apply-template)

(describe make-jid)

(describe make-packet
  (do-it "should return a packet"
    (with-environment :test
      (let [user (model.user/create (factory User))
            packet-map {:to (make-jid user)
                        :from (make-jid user)
                        :body (make-element "pubsub")}
            response (make-packet packet-map)]
        (expect (packet? response))))))

(describe deliver-packet!)
