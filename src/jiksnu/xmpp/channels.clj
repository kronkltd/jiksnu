(ns jiksnu.xmpp.channels
  (:use (ciste [debug :only (spy)]))
  (:gen-class
   :extends tigase.server.AbstractMessageReceiver
   )
  )

(def packet-types-key "packet-types")
(def prepend-text-key "log-prepend")
(def secure-logging-key "secure-logging")

(def packet-types
  (into-array String ["message" "presence" "iq"]))
(def prepend-text "Channel Server")
(def secure-logging false)

;; (defn -getDefaults
;;   [this params]
;;   (let [default-params  (proxy-super params)]
;;     (println "getting defaults")
;;     (.put default-params packet-types-key packet-types)
;;     (.put default-params prepend-text-key prepend-text)
;;     (.put default-params secure-logging-key secure-logging)
;;     default-params)
;;   )

;; (defn -setProperties
;;   [this props]
;;   ;; (println "setting properties")
;;   ;; (proxy-super props)
;;   ;; (let [packet-types (.get props packet-types-key)]
;;   ;;   packet-types
;;   ;;   )
;;   )

(defn -processPacket
  [this packet]
  (println "")
  (println "")
  (println "")
  (println "process packet")
  packet
  
  (println "")
  (println "")
  (println "")

  )
