(ns jiksnu.xmpp
  (:use clj-tigase.core ))

(def ^:dynamic *initial-config*
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

(defn start
  []
  (let [config (get-config *initial-config* tigase-options)]
    (start-router! tigase-options config)))

(defn -main
  []
  (start))
