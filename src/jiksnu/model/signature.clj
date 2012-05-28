(ns jiksnu.model.signature
  (:use (ciste [debug :only [spy]]))
  (:require jiksnu.model
            )
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

;; Crypto functions

