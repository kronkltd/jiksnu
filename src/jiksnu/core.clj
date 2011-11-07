(ns jiksnu.core
  (:use (ciste [config :only [config load-config set-environment!]]))
  (:require (jiksnu [http :as http]
                    [xmpp :as xmpp])
            swank.swank))

(defn start
  []
  (load-config)
  (set-environment! :development)
  (swank.swank/start-repl (or (config :swank :port) "4005"))
  (http/start (or (config :http :port) 8082))
  (xmpp/start)
  @(promise))

(defn -main
  []
  (start))
