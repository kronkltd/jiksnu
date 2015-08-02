(ns jiksnu.model.key
  (:require [clojure.tools.logging :as log]
            [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [slingshot.slingshot :refer [throw+ try+]]
            [validateur.validation :refer [acceptance-of presence-of
                                           validation-set]])
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
           jiksnu.model.Key
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64
           org.apache.commons.io.output.ByteArrayOutputStream
           org.apache.http.impl.client.DefaultHttpClient
           org.bson.types.ObjectId))

(def collection-name "keys")
(def maker #'model/map->Key)
(def default-page-size 20)

(def create-validators
  (validation-set
   ;; (type-of :_id           ObjectId)
))


(def key-factory (KeyFactory/getInstance "RSA"))
(def keypair-generator (KeyPairGenerator/getInstance "RSA"))
(.initialize keypair-generator 1024)

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn get-user
  [key]
  (model.user/fetch-by-id (:userid key)))

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
  [^Key keypair]
  (when keypair
    (format "data:application/magic-public-key,RSA.%s.%s"
            (:n keypair) (:e keypair))))

;; MagicKepPair functions

;; TODO: Actually convert this
(defn pair-hash
  "Convert keypair to a Key"
  [^KeyPair keypair]
  (let [public-key (public-key keypair)
        private-key (private-key keypair)]
    {
     :crt-coefficient  (encode (get-bytes (.getCrtCoefficient private-key)))
     :prime-exponent-p (encode (get-bytes (.getPrimeExponentP private-key)))
     :prime-exponent-q (encode (get-bytes (.getPrimeExponentQ private-key)))
     :prime-p          (encode (get-bytes (.getPrimeP private-key)))
     :prime-q          (encode (get-bytes (.getPrimeQ private-key)))
     :private-exponent (encode (get-bytes (.getPrivateExponent private-key)))
     :n                (encode (get-bytes (.getModulus private-key)))
     :e                (encode (get-bytes (.getPublicExponent private-key)))
     }))

;; Make this an action
(defn get-key-for-user-id
  "Fetch keypair by user id"
  [^ObjectId id]
  (if-let [key (mc/find-one-as-map @_db collection-name {:userid id})]
    (model/map->Key key)
    (log/warnf "Could not find key with id: %s" id)))

(defn get-key-for-user
  [^User user]
  (if (:discovered user)
    (get-key-for-user-id (:_id user))
    (try+
     (throw+ {:message "user is not discovered"})
     (catch Object ex
             (throw+)))))

;; TODO: this should accept a keypair hash
(defn set-armored-key
  "Update keypair with new values"
  [^ObjectId user-id
   ^String n
   ^String e]
  (if-let [key-pair (get-key-for-user-id user-id)]
    (mc/save @_db collection-name
             (merge key-pair
                    {:n n
                     :e e}))
    (mc/insert @_db collection-name
               {:n n
                :e e
                :userid user-id})))

(defn ^PublicKey get-key-from-armored
  [key-pair]
  (let [big-n (-> key-pair :n armored->big-int)
        big-e (-> key-pair :e armored->big-int)
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
