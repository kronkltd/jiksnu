(ns jiksnu.core
  (:use [ciste.config :only (load-config)])
  (:require (jiksnu [http :as http]
                    [model :as model]
                    routes
                    [xmpp :as xmpp])))

(defn start
  ([] (start 8082))
  ([port]
     (load-config)
     (dosync
      (ref-set model/*mongo-database*
               (model/mongo-database*)))
     (http/start port)
     (xmpp/start)))

(defn -main
  []
  (start (Integer/parseInt 8082)))
