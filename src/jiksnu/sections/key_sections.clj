(ns jiksnu.sections.key-sections
  (:use (ciste [sections :only [defsection]])
        (ciste.sections [default :only [full-uri show-section]])
        )
  (:require
            (jiksnu [abdera :as abdera]
                    [namespace :as ns])
            (jiksnu.model [key :as model.key])
            (plaza.rdf [core :as rdf])

            )
  (:import java.math.BigInteger
           jiksnu.model.MagicKeyPair

           )
  )

(defsection show-section [MagicKeyPair :html]
  [key & _]
  [:div
   [:p (:armored-n key)]
   [:p (:armored-e key)]
   ]
  )

(defsection show-section [MagicKeyPair :rdf]
  [key & _]
  (let [user (model.key/get-user key)]
    [(rdf/rdf-resource (str (full-uri user) "#key"))
     [rdf/rdf:type (rdf/rdf-resource (str ns/cert "RSAPublicKey"))
      (rdf/rdf-resource (str ns/cert "identity")) (rdf/rdf-resource (str (full-uri user) "#me"))
      (rdf/rdf-resource (str ns/cert "exponent")) (rdf/l (:public-exponent key))
      (rdf/rdf-resource (str ns/cert "modulus"))  (rdf/rdf-typed-literal
                                                   (.toString
                                                    (BigInteger.
                                                     ^String (:modulus key)) 16)
                                                   (str ns/xsd "#hexBinary"))]]))
