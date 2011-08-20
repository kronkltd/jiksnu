(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :repositories {"jiksnu-internal" "http://build.jiksnu.com/repository/internal"
                 "jiksnu-snapshots" "http://build.jiksnu.com/repository/snapshots"}
  :dependencies [[org.clojure/clojure "1.3.0-beta1"]
                 [org.clojure/tools.logging "0.1.2"]
                 [noir "1.1.1-SNAPSHOT"]
                 [noir-cljs "0.1.0-SNAPSHOT"]
                 [net.kronkltd/lamina "0.4.0-beta2-SNAPSHOT"]
                 [ring/ring-core "0.3.11"]
                 [ring-mock "0.1.1"]
                 [net.kronkltd/ciste "0.1.0-SNAPSHOT"]
                 [compojure "0.6.5"]
                 [net.kronkltd/clj-factory "0.1.0-SNAPSHOT"]
                 [hiccup "0.3.6"]
                 [net.kronkltd/karras "0.6.0"]
                 [closure-templates-clj "0.0.1"]
                 [net.kronkltd/clj-tigase "0.1.0-SNAPSHOT"]
                 [org.apache.abdera/abdera-core "1.1.1"]
                 [org.apache.abdera/abdera-parser "1.1.1"]
                 [org.apache.abdera/abdera-extensions-json "1.1.1"]
                 [org.apache.ws.commons.axiom/axiom-impl "1.2.9"]
                 [net.kronkltd/clj-gravatar "0.0.1"]
                 [com.cliqset/java-salmon-core "0.0.1-SNAPSHOT"]
                 [com.cliqset/java-salmon-simple "0.0.1-SNAPSHOT"]
                 [com.cliqset.abdera/abdera-extensions-activitystreams "0.1.0-SNAPSHOT"]
                 [mysql/mysql-connector-java "5.1.15"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [net.kronkltd/plaza-core "0.0.6-SNAPSHOT"]
                 [net.kronkltd/aleph "0.2.0-beta2-SNAPSHOT"]
                 [org.deri.any23/any23-core "0.5.0"]
                 [org.slf4j/slf4j-simple "1.6.1"]
                 ;; [midje "1.3-alpha1"]
                 [swank-clojure "1.4.0-SNAPSHOT"]]
  :exclusions [org.clojure/contrib
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-jdk14]
  :aot [jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.core]
  :main jiksnu.core
  :warn-on-reflection false
  :jvm-opts ["-server"])
