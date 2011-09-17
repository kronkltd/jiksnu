(ns jiksnu.actions.salmon-actions-test
  (:use clj-factory.core
        clojure.test
        midje.sweet
        (jiksnu core-test model)
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io]
            (jiksnu.actions [user-actions :as actions.user]))
  (:import com.cliqset.magicsig.MagicEnvelope
           jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(defn valid-envelope-stream
  []
  (io/input-stream (io/resource "envelope.xml")))

(deftest test-get-key
  (fact
    (let [user (actions.user/create (factory User))]
      (get-key user)) => nil #_(partial instance? Key)))





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
    (future-fact "should create the message"
      (let [stream (valid-envelope-stream)]
        (process stream)) => true))
  (testing "with an invalid signature"
    (fact "should reject the message"

      )))
