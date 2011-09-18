(ns jiksnu.actions.salmon-actions-test
  (:use clj-factory.core
        clojure.test
        midje.sweet
        (jiksnu core-test model)
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io]
            (jiksnu.actions [user-actions :as actions.user]))
  (:import com.cliqset.magicsig.MagicEnvelope
           java.security.Key
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(defn valid-envelope-stream
  []
  (io/input-stream (io/resource "envelope.xml")))

(deftest test-get-key
  (testing "when the user does not have a key"
    (fact "should return nil"
      (let [user (actions.user/create (factory User))]
        (get-key user)) => nil #_(partial instance? Key)))
  (testing "when there is a key"
    (fact "should return a key"
      (let [user (actions.user/create (factory User))]
        (get-key user))) => (partial instance? Key)))

(deftest test-signature-valid?)

(deftest test-decode-envelope
  (fact "should return a string"
    (let [envelope (stream->envelope (valid-envelope-stream))]
     (decode-envelope envelope) => string?)))

(deftest test-extract-activity
  (fact "should return an activity"
    (let [envelope (stream->envelope (valid-envelope-stream))]
      (extract-activity envelope)) => activity?))

(deftest test-stream->envelope
  (fact "should return an envelope"
    (stream->envelope (valid-envelope-stream)) =>
    (partial instance? MagicEnvelope)))

(deftest test-process
  (testing "with a valid signature"
    (future-fact "should create the message"
      (let [stream (valid-envelope-stream)]
        (process stream)) => truthy))
  (testing "with an invalid signature"
    (fact "should reject the message"

      )))
