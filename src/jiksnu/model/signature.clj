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
           org.openxrd.discovery.impl.BasicDiscoveryManager
           org.openxrd.discovery.impl.HostMetaDiscoveryMethod
           org.openxrd.discovery.impl.HtmlLinkDiscoveryMethod
           org.openxrd.discovery.impl.HttpHeaderDiscoveryMethod))

(def keypair-generator (KeyPairGenerator/getInstance "RSA"))
(.initialize keypair-generator 1024)

(def key-factory (KeyFactory/getInstance "RSA"))

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
  (
   ;; com.sun.org.apache.xml.internal.security.utils.Base64/encode
   Base64/encodeBase64URLSafeString
   byte-array))

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

(def send-key
  "RSA.oidnySWI_e4OND41VHNtYSRzbg5SaZ0YwnQ0J1ihKFEHY49-61JFybnszkSaJJD7vBfxyVZ1lTJjxdtBJzSNGEZlzKbkFvcMdtln8g2ec6oI2G0jCsjKQtsH57uHbPY3IAkBAW3Ar14kGmOKwqoGUq1yhz93rXUomLnDYwz8E88=.AQAB.hgOzTxbqhZN9wce4I7fSKnsJu2eyzP69O9j2UZ56cuulA6_Q4YP5kaNMB53DF32L0ASqHBCM1WXz984hptlT0e4U3asXxqegTqrGPNAXw5A6r2E-9MeS84LDFUnUz420YPxMxknzMJBeAz21PuKyrv_QZf6zmRQ0m5eQ0QNJoYE=")

(def entry
  "<entry xmlns=\"http://www.w3.org/2005/Atom\" xml:lang=\"en\">
  <id>tag:somesite.com,2010-11-05:/username/entry/7k23hk0hjiAlIFjkggmCTjPObFoj5XIbpWrs5iCokMY</id>
  <published>2010-11-05T19:38:27.703Z</published>
  <updated>2010-11-05T19:38:27.703Z</updated>
  <summary type=\"html\">hey, so here is the summary</summary>
  <title type=\"text\">hey, so here is the title</title>
  <author>
    <name>Charlie</name>
    <uri>acct:charlie@domian.com</uri>
  </author>
</entry>")

(def envelope
  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><env xmlns=\"http://salmon-protocol.org/ns/magic-env\"><data type=\"application/atom+xml\">PGVudHJ5IHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDA1L0F0b20iPg0KCTxpZD50YWc6cmV0aWN1bGF0ZW1lLmFwcHNwb3QuY29tLDIwMTAtMTEtMDY6L3NhbG1vbmxpYi9lbnRyeS8wQTBHZFE2cmJpYjdRZ3RfZjE5UTdiUE5hRWVxUVh6NXhNb2FfamN1Ti1BPC9pZD4NCgk8cHVibGlzaGVkPjIwMTAtMTEtMDZUMDE6NDY6NTEuMTQ3WjwvcHVibGlzaGVkPg0KCTx1cGRhdGVkPjIwMTAtMTEtMDZUMDE6NDY6NTEuMTQ3WjwvdXBkYXRlZD4NCgk8c3VtbWFyeSB0eXBlPSJodG1sIj5oZXksIHNvIGhlcmUgaXMgdGhlIHN1bW1hcnk8L3N1bW1hcnk-DQoJPHRpdGxlIHR5cGU9InRleHQiPmhleSwgc28gaGVyZSBpcyB0aGUgdGl0bGU8L3RpdGxlPg0KCTxsaW5rIGhyZWY9Imh0dHA6Ly9yZXRpY3VsYXRlbWUuYXBwc3BvdC5jb20vc2FsbW9ubGliL2VudHJ5LzBBMEdkUTZyYmliN1FndF9mMTlRN2JQTmFFZXFRWHo1eE1vYV9qY3VOLUEiIHR5cGU9InRleHQveGh0bWwiIHJlbD0iYWx0ZXJuYXRlIi8-DQoJPGF1dGhvcj4NCgkJPG5hbWU-U2FsbW9uIExpYnJhcnk8L25hbWU-DQoJCTx1cmk-YWNjdDpzYWxtb25saWJAcmV0aWN1bGF0ZW1lLmFwcHNwb3QuY29tPC91cmk-DQoJPC9hdXRob3I-DQo8L2VudHJ5Pg0KCQkJ</data><encoding>base64url</encoding><alg>RSA-SHA256</alg><sig>psinLK6mpn8IPrKRpta06m49dr2XggN6Bjkbnp3wLwEHClmgwBkwk4Q-3BGbEFxsCR0ogCiTj5JKZbkeR3IkK9bKlEYjMAXWLlrBkDKhfyOitdTbqcCREnd9tRqh562kCF84JY3m1NxPCU1MovMq0zUqryVytAZmgQoEPdzy3Ug=</sig></env>")

(def destination-user
  (URI/create "acct:charlie@reticulateme.appspot.com"))

(defn fetcher
  []
  (com.cliqset.hostmeta.JavaNetXRDFetcher.))

(defn get-salmon
  []
  (Salmon/getDefault))

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
  (try (.signAndDeliver (get-salmon)
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
  [manager method]
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

      ;; (.setParserPool host-meta (BasicParserPool.))
      ;; (.setParserPool header (BasicParserPool.))
      ;; (.setParserPool link (BasicParserPool.))

      ;; (add-discovery-method manager host-meta)
      ;; (add-discovery-method manager header)
      ;; (add-discovery-method manager link)
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
