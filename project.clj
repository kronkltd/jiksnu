(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "http://github.com/duck1123/jiksnu"
  ;; :repositories {
  ;;                "java-dot-net" "http://download.java.net/maven/2"
  ;;                "jiksnu-internal" "http://build.jiksnu.com/repository/internal"
  ;;                "jiksnu-snapshots" "http://build.jiksnu.com/repository/snapshots"
  ;;                "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [
                 ;; [aleph "0.2.1-SNAPSHOT"]
                 [ciste "0.3.0-SNAPSHOT"]
                 [clj-factory "0.2.0"]
                 [clj-tigase "0.1.0-SNAPSHOT"]
                 [clj-time "0.3.5"]
                 [com.newrelic/newrelic-api "1.3.0"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [compojure "1.0.1"]
                 [enlive "1.0.0"]
                 [hiccup "0.3.8"]
                 ;; [joda-time "2.0"]
                 ;; [lamina "0.4.1-SNAPSHOT"]
                 [karras "0.9.0"]
                 [net.kronkltd/clj-gravatar "0.0.1"]
                 [net.kronkltd/plaza "0.0.6-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"
                  :exclusions [org.eclipse.jetty:jetty-server]]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-ext "2.0-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.cache "0.5.0"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/slf4j-log4j12 "1.6.1"]
                 [ring "1.0.2"]
                 [ring-basic-authentication "0.0.1"]
                 [swank-clojure "1.4.0"]
                 [xml-picker-seq "0.0.2"]
                 ]
  :dev-dependencies [
                     [midje "1.3.1" :exclusions [org.clojure/clojure]]
                     [ring-mock "0.1.1"]
                     [lein-cljsbuild "0.0.9" :exclusions [org.apache.ant/ant]]
                     [clj-webdriver "0.6.0-alpha2"]
                     [fluentsoftware/lein-cucumber "1.0.0-SNAPSHOT"]
                     ]
  :java-source-path "src"
  :exclusions [
               com.rabbitmq/amqp-client
               org.apache.abdera/abdera-core
               org.clojure/contrib
               org.clojure/clojure-contrib
               ring/ring-jetty-adapter
               ]
  :aot [
        jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository
        ]
  :cljsbuild {
              :source-path "src-cljs"
              :compiler {
                         :output-to "resources/public/cljs/bootstrap.js"
                         :optimizations :whitespace
                         :pretty-print true
                         }}
  :main ciste.runner
  :newrelic true
  :warn-on-reflection false
  :jvm-opts [
             "-server"
             "-XX:MaxPermSize=1024m"
             "-javaagent:/home/duck/projects/jiksnu/newrelic/newrelic.jar"
             "-Dfile.encoding=UTF-8"
             ])
