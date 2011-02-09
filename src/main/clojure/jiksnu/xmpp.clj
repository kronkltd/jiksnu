(ns jiksnu.xmpp
  (:import tigase.server.XMPPServer
           tigase.server.Packet
           tigase.conf.ConfiguratorAbstract
           tigase.conf.ConfigurationException))

(def #^:dynamic *message-router* (ref nil))

(def #^:dynamic *configurator-prop-key* "tigase-configurator")
(defonce #^:dynamic *default-configurator* "tigase.conf.Configurator")

(def #^:dynamic *name* "Tigase")
(def #^:dynamic *server-name* "message-router")

(def #^:dynamic *initial-config*
     (str
      "tigase.level=ALL\n"
      "tigase.xml.level=INFO\n"
      "handlers=java.util.logging.ConsoleHandler\n"
      "java.util.logging.ConsoleHandler.level=ALL\n"
      "java.util.logging.ConsoleHandler.formatter=tigase.util.LogFormatter\n"))

(def tigase-options
     (into-array
      String
      ["-c"
       "etc/tigase.xml"
       "--property-file"
       "etc/init.properties"]))

;; TODO: support version numbers

(defn get-config
  ([] (get-config tigase-options))
  ([args]
     (ConfiguratorAbstract/loadLogManagerConfig *initial-config*)
     (let [config-class-name (System/getProperty
                              *configurator-prop-key*
                              *default-configurator*)
           ^ConfiguratorAbstract config
           (.newInstance (Class/forName config-class-name))]
       (.init config args)
       (.setName config "basic-conf")
       config)))

(defn get-router
  [args config]
  (let [mr-class-name (.getMessageRouterClassName config)]
    (.newInstance (Class/forName mr-class-name))))

(defmacro with-router
  [router & body]
  `(binding [jiksnu.xmpp.router/*message-router* ~router]
     ~@body))

(defn process!
  [^Packet packet]
  (.processPacket *message-router* packet))

(defn start
  ([] (start tigase-options))
  ([args]
     (dosync
      (ref-set *message-router*
               (let [config (get-config)]
                 (doto (get-router args config)
                   (.setName *server-name*)
                   (.setConfig config)
                   .start))))))

(defn -main
  []
  (start))
