(ns jiksnu.actions.salmon-actions-test
  (:use clojure.test
        midje.sweet
        (jiksnu core-test model)
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io])
  (:import com.cliqset.magicsig.MagicEnvelope))

(use-fixtures :each test-environment-fixture)

(defn valid-envelope-stream
  []
  (io/input-stream (io/resource "envelope.xml")))

(deftest test-get-key
  (fact
    (get-key user) => (partial instance? Key)))





(deftest test-stream->envelope
  (fact "should return an envelope"
    (stream->envelope (valid-envelope-stream)) => (partial instance? MagicEnvelope)
    )
  )

(deftest test-decode-envelope
  (fact "should return a string"
    (let [envelope (stream->envelope (valid-envelope-stream))]
     (decode-envelope envelope) => string?)))

(deftest test-extract-activity
  (fact "should return an activity"
    (let [envelope (stream->envelope (valid-envelope-stream))]
      (extract-activity envelope)) => activity?))

(deftest test-process
  (testing "with a valid signature"
    (fact "should create the message"
      (process (valid-envelope-stream)) => true))
  (testing "with an invalid signature"
    (fact "should reject the message"

      )))
