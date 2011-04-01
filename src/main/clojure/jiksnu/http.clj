(ns jiksnu.http
  (:use ring.adapter.jetty)
  (:require [jiksnu.routes :as routes]))

(def #^:dynamic *future-web* (ref nil))

(defn start
  ([] (start 8082))
  ([port]
     (dosync
      (ref-set *future-web*
               (future
                (run-jetty
                 #'routes/app
                 {:port port
                  ;; :keystore "/home/duck/projects/jiksnu/certs/rsa-keystore"
                  ;; :key-password "GuNgSkOWmWUa46XE1n52vuMPp"
                  :keystore "/home/duck/projects/jiksnu/keystore"
                  :key-password "password"
                  :ssl-port 8443
                  :ssl? true}))))))
