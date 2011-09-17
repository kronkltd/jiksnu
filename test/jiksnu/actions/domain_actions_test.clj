(ns jiksnu.actions.domain-actions-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        (jiksnu model test-helper)
        midje.sweet)
  (:require (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain))

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
