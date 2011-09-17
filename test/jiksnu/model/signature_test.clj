(ns jiksnu.model.signature-test
  (:use clojure.test
        midje.sweet
        jiksnu.core-test
        jiksnu.model.signature

        )
  (:import java.security.KeyPair
           java.security.PrivateKey
           java.security.PublicKey
           java.security.spec.RSAPrivateKeySpec
           java.security.spec.RSAPublicKeySpec

           )
  )

(use-fixtures :each test-environment-fixture)

(deftest test-generate-key
  (fact
    (generate-key) => (partial instance? KeyPair)
    )
  )

(deftest test-public-key
  (fact
    (let [keypair (generate-key)]
     (public-key keypair)) => (partial instance? PublicKey)))

(deftest test-private-key
  (fact
    (let [keypair (generate-key)]
      (private-key keypair)) => (partial instance? PrivateKey)
    )
  )

(deftest test-public-spec)

(deftest test-private-spec)

(deftest test-get-bytes
  (future-fact "should return an array of bytes"
    (let [bigint (BigInteger. "42")]
      (get-bytes bigint)) => nil
    )
  )

(deftest test-encode)

(deftest test-magic-key-string)

(deftest test-pair-hash)

(deftest test-generate-key-for-user)

(deftest test-get-key-for-user-id)

(deftest test-get-key-for-user)

(deftest test-set-armored-key)

(deftest test-drop!)

