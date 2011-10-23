(ns jiksnu.model.signature
  (:use (ciste [debug :only (spy)]))
  (:require jiksnu.model
            [karras.entity :as entity])
  (:import java.net.URI
           java.io.ByteArrayInputStream
           java.io.InputStream
           java.math.BigInteger
           java.security.KeyFactory
           java.security.KeyPair
           java.security.KeyPairGenerator
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

(defn generate-key
  []
  (.genKeyPair keypair-generator))

(defn public-key
  [^KeyPair keypair]
  (.getPublic keypair))

(defn private-key
  [^KeyPair keypair]
  (.getPrivate keypair))

(defn public-spec
  [^KeyPair keypair]
  (.getKeySpec key-factory (public-key keypair) RSAPublicKeySpec))

(defn private-spec
  [^KeyPair keypair]
  (.getKeySpec key-factory (private-key keypair) RSAPrivateKeySpec))

(defn get-bytes
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
        (if (and (not= 0 bitmod)
                 (= (+ bitdiv 1) (/ adjusted-bitlen 8)))
          bigbytes
          (let [start-src (if (= bitmod 0) 1 0)
                biglen2 (if (= bitmod 0) (- biglen 1) biglen)
                start-dst (- (/ adjusted-bitlen 8) biglen2)
                new-size (/ adjusted-bitlen 8)
                resized-bytes (byte-array new-size)]
            (println (class bigbytes))
            (System/arraycopy
             bigbytes start-src resized-bytes
             start-dst biglen2)
            resized-bytes))))))

(defn encode
  [byte-array]
  (Base64/encodeBase64URLSafeString byte-array))

(defn magic-key-string
  [^MagicKeyPair keypair]
  (if keypair
    (str
     "data:application/magic-public-key,RSA."
     (-> keypair :modulus (BigInteger.) get-bytes encode str)
     "."
     (-> keypair :public-exponent (BigInteger.) get-bytes encode str))))

(defn pair-hash
  [^KeyPair keypair]
  (let [public-key (public-key keypair)
        private-key (private-key keypair)]
    {:crt-coefficient  (str (.getCrtCoefficient private-key))
     :modulus          (str (.getModulus private-key))
     :prime-exponent-p (str (.getPrimeExponentP private-key))
     :prime-exponent-q (str (.getPrimeExponentQ private-key))
     :prime-p          (str (.getPrimeP private-key))
     :prime-q          (str (.getPrimeQ private-key))
     :private-exponent (str (.getPrivateExponent private-key))
     :public-exponent  (str (.getPublicExponent private-key))}))

(defn generate-key-for-user
  [^User user]
  (entity/create
   MagicKeyPair
   (assoc (pair-hash (generate-key))
     :userid (:_id user))))

(defn get-key-for-user-id
  [^ObjectId id]
  (entity/fetch-one MagicKeyPair {:userid id}))

(defn get-key-for-user
  [^User user]
  (if (:discovered user)
    (get-key-for-user-id (:_id user))
    (throw (RuntimeException. "user is not discovered"))))

(defn set-armored-key
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
  []
  (entity/delete-all MagicKeyPair))

(defn get-key
  "The magic key must have 3 segments"
  [key]
  #_(MagicKey. (.getBytes key "UTF-8")))

(defn get-key-from-armored
  [key-pair]
  #_(MagicKey. "RSA" (:armored-n key-pair)
             (:armored-e key-pair)))

(defn sign-and-deliver
  "entry is an atom entry"
  [entry key user]
  #_(try
    (.signAndDeliver
     salmon
     (.getBytes entry "UTF-8")
     key
     user)
    (catch Exception e
      (.printStackTrace e))))

(defn get-envelope
  "data is the xml signature"
  [^String data]
  #_(let [me (MagicEnvelope.)]
      (.setData me data)
      me))

(defn serialize
  "Returns an XML string representing the envelope"
  [envelope]
  #_(let [serializer (XMLMagicEnvelopeSerializer.)
        os (ByteArrayOutputStream.)]
    (.serialize serializer envelope os)
    (.toString os "UTF-8")))

(defn get-endpoint
  [uri]
  
  )

(defn discover
  [^String url]
  #_(.discover *discovery-manager* (URI. url)))

#_(defn get-deserializer
  []
  #_(-> (MagicEnvelopeSerializationProvider/getDefault)
      (.getDeserializer MagicSigConstants/MEDIA_TYPE_MAGIC_ENV_XML)))

(defn deserialize
  [^InputStream input-stream]
  ;; TODO: Implement
  )

(defn verify
  [msg]
  ;; TODO: implement
  )

(defn get-verified-data
  [envelope]
  (let [is (ByteArrayInputStream. (.getBytes envelope "UTF-8"))
        m (deserialize is)
        verified-data (verify m)]
    (String. verified-data "UTF-8")))
