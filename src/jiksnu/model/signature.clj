(ns jiksnu.model.signature
  (:use (ciste [debug :only [spy]]))
  (:require jiksnu.model
            [karras.entity :as entity])
  (:import java.net.URI
           java.io.ByteArrayInputStream
           java.io.InputStream
           java.math.BigInteger
           java.security.KeyFactory
           java.security.KeyPair
           java.security.KeyPairGenerator
           java.security.PrivateKey
           java.security.KeyStore
           java.security.PublicKey
           java.security.Signature
           java.security.spec.RSAPrivateKeySpec
           java.security.spec.RSAPublicKeySpec
           jiksnu.model.MagicKeyPair
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64
           org.apache.commons.io.output.ByteArrayOutputStream
           org.apache.http.impl.client.DefaultHttpClient
           org.bson.types.ObjectId))

(def key-factory (KeyFactory/getInstance "RSA"))
(def keypair-generator (KeyPairGenerator/getInstance "RSA"))
(.initialize keypair-generator 1024)

;; Crypto functions

(defn get-keystore
  ([] (get-keystore "JKS"))
  ([type] (KeyStore/getInstance type)))

(defn ^KeyPair generate-key
  "Generates a new RSA keypair"
  []
  (.genKeyPair keypair-generator))

(defn ^PublicKey public-key
  "Extracts the public key from the keypair"
  [^KeyPair keypair]
  (.getPublic keypair))

(defn ^PrivateKey private-key
  [^KeyPair keypair]
  (.getPrivate keypair))

(defn public-spec
  [^KeyPair keypair]
  (.getKeySpec key-factory (public-key keypair) RSAPublicKeySpec))

(defn private-spec
  [^KeyPair keypair]
  (.getKeySpec key-factory (private-key keypair) RSAPrivateKeySpec))


;; Base64 functions


(defn ^String encode
  "Encode the byte array as a url-safe base-64 string"
  [^"[B" byte-array]
  (Base64/encodeBase64URLSafeString byte-array))

(defn ^"[B" decode
  "Decode the base64 string into a byte array."
  [^String data]
  (Base64/decodeBase64 data))

(defn ^BigInteger armored->big-int
  "converts an armored string to a BigInteger"
  [^String armored]
  (-> armored decode BigInteger.))

(defn ^"[B" get-bytes
  "Adapted from the java-salmon implementation"
  [^BigInteger bigint]
  (let [bitlen (.bitLength bigint)
        adjusted-bitlen (-> bitlen
                            (+ 7)
                            (bit-shift-right 3)
                            (bit-shift-left 3))]
    (if (< adjusted-bitlen bitlen)
      (throw (IllegalArgumentException. "Illegal bit len."))
      (let [bigbytes (.toByteArray bigint)
            biglen (alength bigbytes)
            bitmod  (mod bitlen 8)
            bitdiv (/ bitlen 8)]
        (if (and (not (zero? 0))
                 (= (inc bitdiv) (/ adjusted-bitlen 8)))
          bigbytes
          (let [start-src (if (zero? bitmod) 1 0)
                biglen2 (if (zero? bitmod) (dec biglen) biglen)
                start-dst (- (/ adjusted-bitlen 8) biglen2)
                new-size (/ adjusted-bitlen 8)
                resized-bytes (byte-array new-size)]
            (System/arraycopy
             bigbytes start-src resized-bytes
             start-dst biglen2)
            resized-bytes))))))











(defn get-base-string
  "Generate a signature base string"
  [^String armored-data
   ^String datatype
   ^String encoding
   ^String alg]
  (str armored-data "." datatype "." encoding "." alg))

(defn magic-key-string
  "Format keypair as a key string for use in webfinger."
  [^MagicKeyPair keypair]
  (if keypair
    (str
     "data:application/magic-public-key,RSA."
     (-> keypair :modulus (BigInteger.) get-bytes encode str)
     "."
     (-> keypair :public-exponent (BigInteger.) get-bytes encode str))))


;; MagicKepPair functions


;; TODO: Actually convert this
(defn pair-hash
  "Convert keypair to a MagicKeyPair"
  [^KeyPair keypair]
  (let [public-key (public-key keypair)
        private-key (private-key keypair)]
    {
     :crt-coefficient          (str (.getCrtCoefficient private-key))
     :armored-crt-coefficient  (encode (get-bytes (.getCrtCoefficient private-key)))
     
     :prime-exponent-p         (str (.getPrimeExponentP private-key))
     :armored-prime-exponent-p (encode (get-bytes (.getPrimeExponentP private-key)))
     
     :prime-exponent-q         (str (.getPrimeExponentQ private-key))
     :armored-prime-exponent-q (encode (get-bytes (.getPrimeExponentQ private-key)))
     
     :prime-p                  (str (.getPrimeP private-key))
     :armored-prime-p          (encode (get-bytes (.getPrimeP private-key)))
     
     :prime-q                  (str (.getPrimeQ private-key))
     :armored-prime-q          (encode (get-bytes (.getPrimeQ private-key)))
     
     :private-exponent         (str (.getPrivateExponent private-key))
     :armored-private-exponent (encode (get-bytes (.getPrivateExponent private-key)))
     
     :modulus                  (str (.getModulus private-key))
     :armored-n                (encode (get-bytes (.getModulus private-key)))

     :public-exponent          (str (.getPublicExponent private-key))
     :armored-e                (encode (get-bytes (.getPublicExponent private-key)))
     }))

;; Make this an action
(defn generate-key-for-user
  "Generate key for the user and store the result."
  [^User user]
  (entity/create
   MagicKeyPair
   (assoc (pair-hash (generate-key))
     :userid (:_id user))))

(defn get-key-for-user-id
  "Fetch keypair by user id"
  [^ObjectId id]
  (entity/fetch-one MagicKeyPair {:userid id}))

(defn get-key-for-user
  [^User user]
  (if (:discovered user)
    (get-key-for-user-id (:_id user))
    (throw (RuntimeException. "user is not discovered"))))

;; TODO: this should accept a keypair hash
(defn set-armored-key
  "Update keypair with new values"
  [^ObjectId user-id
   ^String n
   ^String e]
  (if-let [key-pair (get-key-for-user-id user-id)]
    (entity/save
     (merge key-pair
            {:armored-n n
             :armored-e e}))
    (entity/create
     MagicKeyPair
     {:armored-n n
      :armored-e e
      :userid user-id})))

(defn drop!
  "Drop all keypairs"
  []
  (entity/delete-all MagicKeyPair))



(defn ^PublicKey get-key-from-armored
  [key-pair]
  (let [big-n (-> key-pair :armored-n armored->big-int)
        big-e (-> key-pair :armored-e armored->big-int)
        key-factory (KeyFactory/getInstance "RSA")
        key-spec (RSAPublicKeySpec. big-n big-e)]
    (.generatePublic key-factory key-spec)))

(defn sign
  "Signs the data with the private key and returns the result"
  [ ^"[B" data ^PrivateKey priv-key]
  (let [^Signature sig (Signature/getInstance "SHA256withRSA")]
    (doto sig
      (.initSign priv-key)
      (.update data))
    (.sign sig)))

(defn verified?
  "Returns true if the signature is valid for the data and public key"
  [^"[B" data ^"[B" signature ^PublicKey key]
  ;; TODO: assert key is RSA
  (let [^Signature sig (Signature/getInstance "SHA256withRSA")]
    (doto sig
      (.initVerify key)
      (.update data))
    (.verify sig signature)))
