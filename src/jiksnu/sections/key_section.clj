(ns jiksnu.sections.key-sections)

(defsection show-section [MagicKeyPair :html]
  [key & _]
  [(rdf/rdf-resource (str (full-uri user) "#key"))
   [rdf/rdf:type (rdf/rdf-resource (str ns/cert "RSAPublicKey"))
    (rdf/rdf-resource (str ns/cert "identity")) (rdf/rdf-resource (str (full-uri user) "#me"))
    (rdf/rdf-resource (str ns/cert "exponent")) (rdf/l (:public-exponent mkp))
    (rdf/rdf-resource (str ns/cert "modulus")) (rdf/rdf-typed-literal
                                                (.toString
                                                 (BigInteger.
                                                  (:modulus mkp)) 16)
                                                (str ns/xsd "#hexBinary"))]])
