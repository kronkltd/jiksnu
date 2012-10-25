(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[aleph "0.3.0-SNAPSHOT"
                  :exclusions [lamina]]
                 [cheshire "4.0.2"]
                 [ciste/ciste-core "0.4.0-SNAPSHOT"
                  :exclusions [joda-time
                               xalan]]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"
                  :exclusions [aleph
                               lamina]]
                 [ciste/ciste-service-tigase "0.4.0-SNAPSHOT"]
                 [ciste/ciste-service-swank "0.4.0-SNAPSHOT"]
                 [clj-factory "0.2.1"]
                 [clojurewerkz/route-one "1.0.0-SNAPSHOT"]
                 [clojurewerkz/urly "1.0.0"
                  :exclusions [com.google.guava/guava]]
                 [clojurewerkz/quartzite "1.0.0-rc5"]
                 [clojurewerkz/elastisch "1.0.0-beta2"
                  :exclusions [com.google.guava/guava]]
                 [com.stuartsierra/lazytest "1.2.3"]
                 [clj-statsd "0.3.2"]
                 [clojurewerkz/mailer "1.0.0-alpha3"]
                 [ clojure-complete "0.2.1"]
                 [clojurewerkz/support "0.7.0-alpha3"
                  :exclusions [com.google.guava/guava]]
                 [com.cemerick/friend "0.0.9"
                  :exclusions [net.sourceforge.nekohtml/nekohtml]]
                 [com.novemberain/monger "1.2.0-alpha1"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [hiccup "1.0.0"]
                 [jayq "0.1.0-alpha4"]
                 [lolg "0.1.0-SNAPSHOT"
                  :exclusions [org.clojure/google-closure-library]]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/plaza "0.2.0-SNAPSHOT"]
                 [net.kronkltd/waltz "0.1.2-SNAPSHOT"
                  :exclusions [org.clojure/google-closure-library]]
                 [oauthentic "0.0.6"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"
                  :exclusions [org.apache.httpcomponents/httpmime
                               com.google.guava/guava]]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"
                   :exclusions [xml-apis]]
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
                 [slingshot "0.10.3"]]
  ;; :exclusions [org.clojure/google-closure-library]
  :aot [
        jiksnu.model
        jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository
        ]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:repl-listen-port 9001
              :builds
              [
               {:source-path "src-cljs"
                :compiler
                {:output-to "resources/public/assets/js/jiksnu.js"
                 :output-dir "target/cljsout/simple"
                 :optimizations :whitespace
                 :pretty-print true
                 :externs ["resources/externs/backbone-0.9.1.js"
                           "resources/externs/underscore-0.3.1.js"]

                 ;; :foreign-libs [{:file "public/assets/js/underscore-1.3.3.js"
                 ;;                 :provides ["_"]}
                 ;;                {:file "public/assets/js/backbone-0.9.2.js"
                 ;;                 :provides ["Backbone"]}]

                 }}
               #_{:source-path "src-cljs"
                :compiler
                {

                 :output-to "resources/public/assets/js/jiksnu.min.js"
                 :externs ["externs/backbone-0.9.1.js"
                           "externs/underscore-0.3.1.js"]

                 :foreign-libs [{:file "public/assets/js/underscore-1.3.3.js"
                                 :provides ["_"]}
                                {:file "public/assets/js/backbone-0.9.2.min.js"
                                 :provides ["Backbone"]}]



                 :output-dir "target/cljsout/min"
                 :optimizations :simple

                 }}
               #_{:source-path "src-cljs"
                :compiler
                  {
                   :output-to "resources/public/assets/js/jiksnu.advanced.js"
                   :externs [
                             "resources/externs/backbone-0.9.1.js"
                             "resources/externs/underscore-0.3.1.js"
                             "resources/externs/jquery-1.7.js"
                             "resources/public/assets/js/knockout-2.1.0.js"
                             ]
                   :output-dir "target/cljsout/advanced"
                   :pretty-print false
                   :optimizations :advanced
                 :foreign-libs [{:file "public/assets/js/underscore-1.3.3.js"
                                 :provides ["_"]}
                                {:file "public/assets/js/backbone-0.9.2.min.js"
                                 :provides ["Backbone"]}]

                   }}]}
  :main ciste.runner
  :jvm-opts [
             "-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"
             ]
  :repositories {
                 "stuart" "http://stuartsierra.com/maven2"
                 "sonatype-oss-public"   "https://oss.sonatype.org/content/groups/public/"
                 "apache-repo-snapshots" "https://repository.apache.org/content/repositories/snapshots"
                 }
  :warn-on-reflection false

  :profiles {:dev
             {:resource-paths ["test-resources"]
              :dependencies
              [[midje "1.5.0-SNAPSHOT"
                 :exclusions [joda-time]]
               [ring-mock "0.1.3"]
               [clj-webdriver "0.6.0-alpha11"]]}}
  :lesscss-output-path "resources/public/assets/themes/classic/"

  :plugins [[lein-cljsbuild "0.2.7"]
            [codox "0.6.1"]
            [lein-cucumber "1.0.0"]  
            [lein-lesscss "1.2"]
            [lein-midje "2.0.0-SNAPSHOT"]]
  )
