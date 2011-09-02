(ns jiksnu.model.signature
  (:use ciste.debug)
  (:require jiksnu.model
            [karras.entity :as entity])
  (:import com.cliqset.magicsig.MagicEnvelope
           com.cliqset.magicsig.MagicEnvelopeSerializationProvider
           com.cliqset.magicsig.MagicKey
           com.cliqset.magicsig.MagicSigConstants
           com.cliqset.magicsig.xml.XMLMagicEnvelopeSerializer
           com.cliqset.salmon.Salmon
           com.cliqset.salmon.HostMetaSalmonEndpointFinder
           java.net.URI
           java.io.ByteArrayInputStream
           java.math.BigInteger
           java.security.KeyFactory
           java.security.KeyPair
           java.security.KeyPairGenerator
           java.security.spec.RSAPrivateKeySpec
           java.security.spec.RSAPublicKeySpec
           jiksnu.model.MagicKeyPair
           org.apache.commons.codec.binary.Base64
           org.apache.commons.io.output.ByteArrayOutputStream
           org.apache.http.impl.client.DefaultHttpClient
           org.opensaml.xml.parse.BasicParserPool
           org.openxrd.DefaultBootstrap
           org.openxrd.discovery.DiscoveryManager
           org.openxrd.discovery.DiscoveryMethod
           org.openxrd.discovery.impl.BasicDiscoveryManager
           org.openxrd.discovery.impl.HostMetaDiscoveryMethod
           org.openxrd.discovery.impl.HtmlLinkDiscoveryMethod
           org.openxrd.discovery.impl.HttpHeaderDiscoveryMethod))

(def keypair-generator (KeyPairGenerator/getInstance "RSA"))
(.initialize keypair-generator 1024)

(def key-factory (KeyFactory/getInstance "RSA"))
(def salmon (Salmon/getDefault))
(def fetcher (com.cliqset.hostmeta.JavaNetXRDFetcher.))


(defn generate-key
  []
  (.genKeyPair keypair-generator))

(defn public-key
  [^KeyPair keypair]
  (.getPublic keypair))

(defn private-key
  [^RSAPrivateKeySpec keypair]
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
  [^KeyPair keypair]
  (str
   "data:application/magic-public-key,RSA."
   (str (encode (get-bytes (BigInteger. (:modulus keypair)))))
   "."
   (str (encode (get-bytes (BigInteger. (:public-exponent keypair)))))))

(defn pair-hash
  [keypair]
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
  [user]
  (entity/create
   MagicKeyPair
   (assoc (pair-hash (generate-key))
     :userid (:_id user))))

(defn get-key-for-user-id
  [id]
  (entity/fetch-one MagicKeyPair {:userid id}))

(defn get-key-for-user
  [user]
  (get-key-for-user-id (:_id user)))

(defn set-armored-key
  [user-id n e]
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
  (MagicKey. (.getBytes key "UTF-8")))

(defn get-key-from-armored
  [key-pair]
  (MagicKey. "RSA" (:armored-n key-pair)
             (:armored-e key-pair)))

(defn sign-and-deliver
  "entry is an atom entry"
  [entry key user]
  (try
    (.signAndDeliver
     salmon
     (.getBytes entry "UTF-8")
     key
     user)
    (catch Exception e
      (.printStackTrace e))))

(defn get-envelope
  "data is the xml signature"
  ([]
     (get-envelope "foo"))
  ([^String data]
     (let [me (MagicEnvelope.)]
       (.setData me data)
       me)))

(defn serialize
  [^MagicEnvelope envelope]
  (let [serializer (XMLMagicEnvelopeSerializer.)
        os (ByteArrayOutputStream.)]
    (.serialize serializer envelope os)
    (.toString os "UTF-8")))

(defn get-endpoint
  [uri]
  
  )

(defn add-discovery-method
  [^DiscoveryManager manager
   ^DiscoveryMethod method]
  (.add (.getDiscoveryMethods manager) method))

(defn get-discovery-manager
  []
  (DefaultBootstrap/bootstrap)
  (let [http-client (DefaultHttpClient.)
        manager (BasicDiscoveryManager.)]
    (let [host-meta (HostMetaDiscoveryMethod.)
          header (HttpHeaderDiscoveryMethod.)
          link (HtmlLinkDiscoveryMethod.)]

      (.setHttpClient host-meta http-client)
      (.setHttpClient header http-client)
      (.setHttpClient link http-client)

      (.setParserPool host-meta (BasicParserPool.))
      (.setParserPool header (BasicParserPool.))
      (.setParserPool link (BasicParserPool.))

      (add-discovery-method manager host-meta)
      (add-discovery-method manager header)
      (add-discovery-method manager link)
      )
    manager))

(defn get-deserializer
  []
  (-> (MagicEnvelopeSerializationProvider/getDefault)
      (.getDeserializer MagicSigConstants/MEDIA_TYPE_MAGIC_ENV_XML)))

(defn get-verified-data
  [^Salmon salmon ^MagicEnvelope envelope]
  (let [is (ByteArrayInputStream. (.getBytes envelope "UTF-8"))
        m (.deserialize (get-deserializer) is)
        verified-data (.verify salmon m)]
    (String. verified-data "UTF-8")))
