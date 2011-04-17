(ns jiksnu.config
  (:use ciste.config))

(defonce #^:dynamic *debug* false)

(dosync
 (ref-set
  *environments*
  {:development {:database {:host "localhost"
                            :name :jiksnu_development}
                 :domain "beta.jiksnu.com"
                 :registration-enabled false
                 :debug false
                 :print {:request true
                         :matchers true
                         :predicates true
                         :params false}}
   :test {:domain "test.jiksnu.com"
          :registration-enabled false
          :database {:host "localhost"
                     :name :jiksnu_test}}}))

(defmacro with-debug
  [& body]
  `(binding [jiksnu.config/*debug* true]
     ~@body))
