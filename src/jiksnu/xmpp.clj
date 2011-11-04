(ns jiksnu.xmpp
  (:use (ciste [config :only [config definitializer]]
               [debug :only [spy]])
        clj-tigase.core)
  (:require (clojure [string :as string])))

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
       "--auth-db" "jiksnu.xmpp.user_repository"
       "--user-db" "jiksnu.xmpp.user_repository"
       "--debug" "server"
       "--sm-plugins" "jiksnu,message"
       "--c2s-ports" (str (config :xmpp :c2s))
       "--s2s" (str (config :xmpp :s2s))
       "--virt-hosts" (config :domain)
       "--comp-name-1" "channels"
       "--comp-class-1" "jiksnu.xmpp.channels"]))

(defn start
  []
  (let [tigase-config (get-config *initial-config* (tigase-options))]
    (spy (bean tigase-config))
    (start-router! tigase-options tigase-config)))
