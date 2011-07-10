(ns jiksnu.core
  (:use [ciste.config :only (load-config)])
  (:require [jiksnu.http :as http]
            jiksnu.routes
            [jiksnu.xmpp :as xmpp]))

(defn start
  ([] (start 8082))
  ([port]
     (load-config)
     (http/start port)
     (xmpp/start)))

(defn -main
  []
  (start (Integer/parseInt 8082)))
