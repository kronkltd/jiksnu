(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [
                 [cheshire "4.0.0"]
                 [ciste/ciste-core "0.4.0-SNAPSHOT"]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-tigase "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-swank "0.4.0-SNAPSHOT"]
                 [clj-factory "0.2.1-SNAPSHOT"]
                 [clojurewerkz/route-one "1.0.0-beta1"]
                 [clojurewerkz/urly "1.0.0"]
                 [clojurewerkz/quartzite "1.0.0-rc5"]
                 [clojurewerkz/elastisch "1.0.0-beta2"]
                 [clojurewerkz/mailer "1.0.0-alpha3"]
                 [clojurewerkz/support "0.7.0-alpha3"]
                 [com.cemerick/friend "0.0.9"]
                 [com.novemberain/monger "1.2.0-alpha1"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [hiccup "1.0.0"]
                 [jayq "0.1.0-alpha4"]
                 [lamina "0.5.0-alpha3"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/plaza "0.1.0-SNAPSHOT"]
                 [net.kronkltd/waltz "0.1.2-SNAPSHOT"]
                 [oauthentic "0.0.6"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-ext "2.0-SNAPSHOT"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/core.cache "0.5.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [ring "1.1.1"]
                 [ring/ring-core "1.1.1"]
                 [ring-basic-authentication "0.0.1"]
                 [slingshot "0.10.2"]
                 ]
  :exclusions [org.clojure/google-closure-library]
  :aot [
        jiksnu.model
        jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository
        ]
  :cljsbuild {:repl-listen-port 9001
              :builds
              [
               {:source-path "src-cljs"
                :compiler
                {:output-to "resources/public/assets/js/jiksnu.js"
                 :output-dir "target/cljsout/simple"
                 :optimizations :simple
                 :pretty-print true}}
               #_{:source-path "src-cljs"
                :compiler
                {:output-to "resources/public/assets/js/jiksnu.min.js"
                 :output-dir "target/cljsout/min"
                 :optimizations :simple}}
               #_{:source-path "src-cljs"
                :compiler
                {:output-to "resources/public/assets/js/jiksnu.advanced.js"
                 :output-dir "target/cljsout/advanced"
                 :optimizations :advanced                }}]}
  :main ciste.runner
  :jvm-opts [
             "-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"
             ]
  :repositories {
                 ;; "java-dot-net"          "http://download.java.net/maven/2"
                 "sonatype-oss-public"   "https://oss.sonatype.org/content/groups/public/"
                 "apache-repo-snapshots" "https://repository.apache.org/content/repositories/snapshots"
                 }
  :warn-on-reflection false

  :profiles {:dev
             {
              :dependencies
              [[midje "1.4.0"]
               [ring-mock "0.1.1"]
               [clj-webdriver "0.6.0-alpha10"]]}}

  :plugins [
               [lein-cljsbuild "0.2.4"]
               [lein-cucumber "1.0.0"]  
               [lein-midje "2.0.0-SNAPSHOT"]

            ]
  )
