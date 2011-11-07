(ns jiksnu.actions.domain-actions-test
  (:use (clj-factory [core :only [factory fseq]])
        clojure.test
        (jiksnu model core-test)
        jiksnu.actions.domain-actions
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain))

(use-fixtures :once test-environment-fixture)

(deftest test-check-webfinger)

(deftest test-create
  (fact "should create the domain"
    (let [options {:_id (fseq :domain)}]
      (create options) => domain?)))

(deftest test-delete
  (testing "when the domain does not exist"
    (fact "should return nil"
      (let [domain (factory Domain)]
        (delete domain) => nil?)))
  (testing "when the domain exists"
    (against-background
      [(around :facts
         (let [domain (create (factory Domain))]
           ?form))]
      (fact "should return the deleted domain"
        (delete domain) => domain)
      (fact "should delete the domain"
        (delete domain)
        (show domain) => nil?))))

(deftest test-discover-onesocialweb
  (fact "should send a packet to that domain"
    (let [action #'discover
          domain (create (factory Domain))
          id (:_id domain)]
      (discover-onesocialweb domain) => packet/packet?)))

(deftest test-discover)

(deftest test-edit-page)

(deftest test-index)

(deftest test-show)

(deftest test-find-or-create)

(deftest current-domain-test)

(deftest test-ping)

(deftest test-ping-error)

(deftest test-ping-response)

(deftest test-set-xmpp)

(deftest test-get-user-meta-uri)

(deftest test-update)

(deftest test-host-meta
  (fact "should return a XRD object"
    (host-meta) => map?))

