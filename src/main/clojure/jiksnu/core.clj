(ns jiksnu.core
  (:require [jiksnu.http :as http]
            [jiksnu.xmpp :as xmpp]))

(defmacro spy
  [sym]
  `(let [value# ~sym]
     (println (str ~(str sym) ":") value#)
     value#))

;; (defn spy
;;   [variable]
;;   (println (str (name variable) ": " variable)))

(defn start
  ([] (start 8082))
  ([port]
     (http/start port)
     (xmpp/start)))

(defn -main
  []
  (start (Integer/parseInt 8082)))
