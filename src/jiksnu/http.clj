(ns jiksnu.http
  (:use (aleph [http :only [start-http-server]])
        (ciste [config :only [config describe-config]]
               [debug :only [spy]])
        )
  (:require
   (clojure [string :as string])
   (jiksnu [routes :as routes])))

(def ^:dynamic *future-web* (ref nil))

(describe-config [:http :port] :number
  "The port the http server should run on")

(describe-config [:http :websocket] :boolean
  "Should websocket support be enabled?")

(describe-config [:http :handler] :string
  "A string pointing to a fully namespace-qualified http handler")

(defn start
  []
  (let [handler (config :http :handler)]
    (-> handler (string/split #"/")
        first symbol require)
    (let [handler-var (resolve (symbol handler))]
      (start-http-server handler-var
                         ;; #'routes/app
                         {:port (config :http :port)
                          :websocket (config :http :websocket)
                          :join? false}))))
