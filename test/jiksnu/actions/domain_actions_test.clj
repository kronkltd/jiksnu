(ns jiksnu.actions.domain-actions-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        (jiksnu model core-test)
        jiksnu.actions.domain-actions
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain))

(use-fixtures :once test-environment-fixture)

(deftest test-check-webfinger)

(deftest test-create)

(deftest test-delete)

(deftest test-discover)

(deftest test-edit)

(deftest test-index)

(deftest test-show)

(deftest test-find-or-create)

(deftest test-ping)

(deftest test-ping-error)

(deftest test-ping-response)

(deftest test-set-xmpp)

(deftest test-discover-onesocialweb
  (fact "should send a packet to that domain"
    (let [action #'discover
          domain (create (factory Domain))
          id (:_id domain)]
      (discover-onesocialweb domain) => packet/packet?)))

(deftest test-host-meta
  (fact "should return a XRD object"
    (host-meta) => map?))

