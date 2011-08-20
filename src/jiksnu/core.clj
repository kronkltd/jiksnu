(ns jiksnu.core
  (:use [ciste.config :only (load-config config)])
  (:require (jiksnu [http :as http]
                    [model :as model]
                    routes
                    [xmpp :as xmpp])
            swank.swank))

(defn start
  []
  (load-config)
  (swank.swank/start-repl (or (config :swank :port) "4005"))
  (dosync
   (ref-set model/*mongo-database*
            (model/mongo-database*)))
  (http/start (or (config :http :port) 8082))
  (xmpp/start))

(defn -main
  []
  (start))
