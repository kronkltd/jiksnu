(ns jiksnu.config)

(defonce #^:dynamic *current-environment* :test)

(defonce #^:dynamic *debug* false)

(defonce #^:dynamic *environments*
  (ref {:development {:database {:host "localhost"
                                 :name :jiksnu_development}
                      :domain "beta.jiksnu.com"
                      :print {:request true
                              :params false}}
        :test {:domain "test.jiksnu.com"
               :database {:host "localhost"
                          :name :jiksnu_test}}}))

(defmacro with-environment
  [environment & body]
  `(binding [jiksnu.config/*current-environment* environment]
     ~@body))

(defn config
  []
  (get @*environments* *current-environment*))

(defmacro with-debug
  [& body]
  `(binding [jiksnu.config/*debug* true]
     ~@body))
