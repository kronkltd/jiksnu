(ns jiksnu.model.signature-test
  (:use clojure.test
        midje.sweet
        jiksnu.core-test)

  )

(deftest test-generate-key
  (fact
    (generate-key) => nil #_(partial instance? Key)
    )
  )

(deftest test-public-key
  (fact
    (public-key .keypair.) => nil #_(partial instance? RSAPublicKeySpec)))

(deftest test-private-key
  (fact
    (private-key .keypair.) => nil
    )
  )

(deftest test-public-spec)

(deftest test-private-spec)

(deftest test-get-bytes
  (fact
    (let [bigint (BigInteger. )]
      (get-byte bigint)) => nil
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

(deftest test-fetcher
  (fact
    (fetcher) => nil #_(partial instance? JavaNetXRDFetcher)
    )
  )

