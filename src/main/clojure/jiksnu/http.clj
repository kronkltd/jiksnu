(ns jiksnu.http
  (:use ring.adapter.jetty)
  (:require [jiksnu.http.routes :as routes]))

(def #^:dynamic *future-web*
     (ref nil))

(defn start
  ([] (start 8082))
  ([port]
     (dosync
      (ref-set *future-web*
               (future
                (run-jetty
                 (#'routes/app)
                 {:port port
                  :keystore "certs/rsa-keystore"
                  :key-password ""
                  :ssl? true}))))))
