(ns jiksnu.sections.key-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri show-section index-line]]
        [jiksnu.sections :only [action-link admin-index-line admin-index-block
                                format-links admin-index-section bind-property
                                dump-data control-line pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.model.key :as model.key]
            [plaza.rdf.core :as rdf])
  (:import java.math.BigInteger
           jiksnu.model.Key))

(defsection admin-index-block [Key :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection show-section [Key :html]
  [key & _]
  ;; TODO: Rdfa
  [:div
   [:p (:n key)]
   [:p (:e key)]])

(defsection show-section [Key :rdf]
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

(defsection show-section [Key :viewmodel]
  [activity & [page]]
  (select-keys activity [:_id :n :e :userid]))

