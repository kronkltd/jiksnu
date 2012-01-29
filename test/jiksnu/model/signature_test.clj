(ns jiksnu.model.signature-test
  (:use (ciste [debug :only [spy]])
        (clj-factory [core :only [factory]])
        clojure.test
        midje.sweet
        jiksnu.test-helper
        jiksnu.model.signature)
  (:require (clojure.java [io :as io])
            (jiksnu.actions [salmon-actions :as actions.salmon]
                            [user-actions :as actions.user])
            (karras [entity :as entity]))
  (:import java.security.Key
           java.security.KeyPair
           java.security.PrivateKey
           java.security.PublicKey
           java.security.spec.RSAPrivateKeySpec
           java.security.spec.RSAPublicKeySpec
           jiksnu.model.MagicKeyPair
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64))

(test-environment-fixture)

(def armored-n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0=")
(def armored-e "AQAB")

(defn valid-envelope-stream
  []
  (io/input-stream (io/resource "envelope.xml")))

(defn byte-array?
  "Returns true if the object is a byte array"
  [o]
  (= (type o) (type (byte-array []))))

;; (deftest test-generate-key)

(fact "should return a KeyPair"
  (generate-key) => (partial instance? KeyPair))

;; (deftest test-public-key)

(fact "should return a public key"
  (let [keypair (generate-key)]
    (public-key keypair)) => (partial instance? PublicKey))

;; (deftest test-private-key)

(fact "should return a private key"
  (let [keypair (generate-key)]
    (private-key keypair)) => (partial instance? PrivateKey))

;; (deftest test-public-spec)

;; (deftest test-private-spec)

;; (deftest test-get-bytes)

(fact "should return an array of bytes"
  (let [bigint (BigInteger. "42")]
    (get-bytes bigint)) => byte-array?)

;; (deftest test-encode)

(fact "should return a string"
  (let [data (.getBytes "foo")]
    (encode data) => string?))

;; (deftest test-decode)

(fact "should decode to the encoded data"
  (let [msg "foo"
        data (.getBytes msg)
        armored-string (encode data)]
    (String. (decode armored-string) "utf-8") => msg))

;; (deftest test-magic-key-string)

;; (deftest test-pair-hash)

;; (deftest test-generate-key-for-user)

;; (deftest test-get-key-for-user-id)

;; (deftest test-get-key-for-user)

(fact "when the user has a key"
  (fact "should return that key"
    (let [user (actions.user/create (factory User {:discovered true}))
          keypair (generate-key-for-user user)]
      (get-key-for-user user) => keypair)))
(fact "when the user does not have a key"
  (fact "should return nil"
    (let [user (actions.user/create (factory User {:discovered true}))]
      (get-key-for-user user)) => nil))

;; (deftest test-set-armored-key)

;; (deftest test-drop!)

(fact "when there are keys"
  (fact "should delete all the keys"
    (let [user (actions.user/create (factory User))
          mkp (generate-key-for-user user)]
      (entity/count-instances MagicKeyPair) => 1
      (drop!)
      (entity/count-instances MagicKeyPair) => 0)))

;; (deftest test-get-key)

;; (deftest test-get-key-from-armored)

(fact "should return a public key"
  (let [key-pair {:armored-n armored-n
                  :armored-e armored-e}]
    (get-key-from-armored key-pair) => #(instance? PublicKey %)))

;; (deftest test-sign-and-deliver)

;; (deftest test-get-envelope)

;; (deftest test-serialize)

;; (deftest test-sign)

(fact "should return a byte array"
  (let [key-pair (generate-key)
        priv-key (private-key key-pair)
        data (.getBytes "foo")]
    (sign data priv-key) => byte-array?))

;; (deftest test-verified?)

(fact "when using known keys"
  (fact "should verify"
    (let [kp (generate-key)
          pub-key (public-key kp)
          priv-key (private-key kp)
          data (.getBytes "foo")
          sig (sign data priv-key)]
      (verified? data sig pub-key))))

(fact "when the signature is valid"
  (fact "should return a valid result"
    (let [envelope (actions.salmon/stream->envelope (valid-envelope-stream))
          public-key (get-key-from-armored {:armored-n armored-n
                                            :armored-e armored-e})
          data (-> envelope :data Base64/decodeBase64)
          signature (-> envelope :sig Base64/decodeBase64)]
      (verified? data signature public-key) => nil)))
