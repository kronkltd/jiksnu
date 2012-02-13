(ns jiksnu.xmpp
  (:use (ciste [config :only [config describe-config]]
               [debug :only [spy]])
        clj-tigase.core)
  (:require (clojure [string :as string])))


;; TODO: Pull this list from a UserRole collection
(describe-config [:admins]
  :list
  "A list of usernames that are considered admins of the system.")

(describe-config [:xmpp :c2s]
  :number
  "The client to server port for the xmpp service")

(describe-config [:xmpp :s2s]
  :number
  "The server to server port for the xmpp service")


(def ^:dynamic *initial-config*
    "" #_(str
      "tigase.level=ALL\n"
      "tigase.xml.level=INFO\n"
      "handlers=java.util.logging.ConsoleHandler\n"
      "java.util.logging.ConsoleHandler.level=ALL\n"
      "java.util.logging.ConsoleHandler.formatter=tigase.util.LogFormatter\n"))

(defn tigase-options
  []
  (into-array
      String
      ["--admins" (->> (config :admins)
                       (map (fn [username]
                              ;; TODO: ensure user created
                              (str username "@" (config :domain))))
                       (string/join "," ))

       ;; Make these configable
       "--auth-db" "jiksnu.xmpp.user_repository"
       "--user-db" "jiksnu.xmpp.user_repository"
       "--debug" "server"

       ;; [:xmpp :plugins] perhaps?
       "--sm-plugins" "jiksnu,message"
       "--c2s-ports" (str (config :xmpp :c2s))
       "--s2s" (str (config :xmpp :s2s))
       "--virt-hosts" (config :domain)

       ;; TODO: Register xmpp components and dynamically generate
       "--comp-name-1" "channels"
       "--comp-class-1" "jiksnu.xmpp.channels"]))

(defn start
  []
  (let [tigase-config (get-config *initial-config* (tigase-options))]
    (spy (bean tigase-config))
    (start-router! tigase-options tigase-config)))
