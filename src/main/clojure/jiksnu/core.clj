(ns jiksnu.core
  (:require [jiksnu.http :as http]
            jiksnu.routes
            [jiksnu.xmpp :as xmpp]))

(defn start
  ([] (start 8082))
  ([port]
     (http/start port)
     (xmpp/start)))

(defn -main
  []
  (start (Integer/parseInt 8082)))
