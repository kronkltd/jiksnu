(ns jiksnu.http
  (:use (aleph [http :only (start-http-server)])
        (ciste [debug :only (spy)]))
  (:require (jiksnu [routes :as routes])))

(def ^:dynamic *future-web* (ref nil))

(defn start
  ([] (start 8082))
  ([port]
     (start-http-server
      #'routes/app
      {:port port
       ;; :websocket true
       :join? false})))
