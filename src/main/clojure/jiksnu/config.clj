(ns jiksnu.config)

(defonce #^:dynamic *current-environment* :development)

(defonce #^:dynamic *debug* false)

(def #^:dynamic *environments*
  (ref {:development {:database {:host "localhost"
                                 :name :jiksnu_development}
                      :domain "beta.jiksnu.com"
                      :registration-enabled false
                      :debug false
                      :print {:request true
                              :params false}}
        :test {:domain "test.jiksnu.com"
               :registration-enabled false
               :database {:host "localhost"
                          :name :jiksnu_test}}}))

(defn config
  []
  (get @*environments* *current-environment*))

(defmacro with-debug
  [& body]
  `(binding [jiksnu.config/*debug* true]
     ~@body))
