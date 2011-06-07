(ns jiksnu.http
  (:use aleph.http
        ciste.debug)
  (:require [jiksnu.routes :as routes]))

(def #^:dynamic *future-web* (ref nil))

(defn start
  ([] (start 8082))
  ([port]
     (start-http-server #'routes/app
      {:port port
       ;; :keystore "/home/duck/projects/jiksnu/certs/rsa-keystore"
       ;; :key-password "GuNgSkOWmWUa46XE1n52vuMPp"
       ;; :keystore "/home/duck/projects/jiksnu/keystore"
       ;; :key-password "password"
       :websocket true
       ;; :ssl-port 8443
       ;; :ssl? true
       :join? false})))
