(ns jiksnu.model.signature-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        midje.sweet
        jiksnu.core-test
        jiksnu.model.signature)
  (:require (jiksnu.actions [user-actions :as actions.user]))
  (:import java.security.KeyPair
           java.security.PrivateKey
           java.security.PublicKey
           java.security.spec.RSAPrivateKeySpec
           java.security.spec.RSAPublicKeySpec
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest test-generate-key
  (fact "should return a KeyPair"
    (generate-key) => (partial instance? KeyPair)))

(deftest test-public-key
  (fact
    (let [keypair (generate-key)]
     (public-key keypair)) => (partial instance? PublicKey)))

(deftest test-private-key
  (fact
    (let [keypair (generate-key)]
      (private-key keypair)) => (partial instance? PrivateKey)))

(deftest test-public-spec)

(deftest test-private-spec)

(deftest test-get-bytes
  (future-fact "should return an array of bytes"
    (let [bigint (BigInteger. "42")]
      (get-bytes bigint)) => nil))

(deftest test-encode)

(deftest test-magic-key-string)

(deftest test-pair-hash)

(deftest test-generate-key-for-user)

(deftest test-get-key-for-user-id)

(deftest test-get-key-for-user
  (testing "when the user has a key"
    (fact "should return that key"
      (let [user (actions.user/create (factory User))
            keypair (generate-key-for-user user)]
        (get-key-for-user user) => keypair)))
  (testing "when the user does not have a key"
    (fact "should return nil"
      (let [user (actions.user/create (factory User))]
        (get-key-for-user user)) => nil)))

(deftest test-set-armored-key)

(deftest test-drop!)

