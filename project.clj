(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[aleph "0.3.0"]
                 [ciste "0.5.0-SNAPSHOT"]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]
                  ]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [ciste/ciste-service-tigase "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [ciste/ciste-service-swank "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [net.kronkltd/clj-airbrake "2.0.1-SNAPSHOT"]
                 [clj-stacktrace "0.2.6"]
                 [clj-statsd "0.3.10"]
                 [clj-time "0.6.0"]
                 [clj-http "0.7.6"]
                 [clojurewerkz/route-one "1.0.0-beta2"]
                 [clojurewerkz/urly "1.0.0"
                  :exclusions [com.google.guava/guava]]
                 [clojurewerkz/mailer "1.0.0-alpha3"]
                 [clojure-complete "0.2.3"]
                 [clojurewerkz/support "0.17.0"]
                 [com.novemberain/monger "1.6.0"]
                 [com.novemberain/validateur "1.5.0"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.7"]
                 [crypto-random "1.1.0"]
                 [hiccup "1.0.4"]
                 [jayq "2.4.0"]
                 [lamina "0.5.0"]
                 [lib-noir "0.5.6"]
                 [lolg "0.1.0-SNAPSHOT"
                  :exclusions [org.clojure/google-closure-library]]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/plaza "0.3.0-SNAPSHOT"]
                 [net.kronkltd/waltz "0.1.2-SNAPSHOT"
                  :exclusions [org.clojure/google-closure-library]]
                 [org.apache.abdera/abdera-client "1.1.3"]
                 [org.bovinegenius/exploding-fish "0.3.3"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.reader "0.7.6"]
                 [org.clojure/data.json "0.2.3"]
                 [org.jsoup/jsoup "1.7.2"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.7.5"]
                 [org.slf4j/slf4j-log4j12 "1.7.5"]
                 [org.webjars/backbonejs "1.0.0-1"]
                 [org.webjars/bootstrap "3.0.0"]
                 [org.webjars/jquery "2.0.3"]
                 [org.webjars/knockout "2.3.0"]
                 [org.webjars/momentjs "2.1.0"]
                 [org.webjars/underscorejs "1.5.1"]
                 [ring "1.2.0-beta3"]
                 [ring/ring-core "1.2.0-beta3"]
                 [ring-basic-authentication "1.0.2"]
                 [rmarianski/tidy-up "0.0.2"]
                 [slingshot "0.10.3"]
                 [tigase/tigase-server "5.2.0-beta1"]
                 [clj-webdriver "0.6.0"]

                 ;; I know you really want to upgrade this one, but
                 ;; you can't. :(
                 [xml-apis "1.4.01"]
                 #_[org.apache.httpcomponents/httpclient "4.2.5"]

]
  ;; :exclusions [org.clojure/google-closure-library]
  :aot [jiksnu.model
        ciste.runner
        jiksnu.modules.xmpp.plugin
        ;; jiksnu.modules.xmpp.channels
        jiksnu.modules.xmpp.user-repository]
  ;; :hooks [leiningen.cljsbuild]
  :cljsbuild {:repl-listen-port 9001
              :builds
              [{:source-paths ["src-cljs"]
                :compiler
                {:output-to "resources/public/assets/js/jiksnu.js"
                 :output-dir "target/cljsout/simple"
                 :optimizations :whitespace
                 :pretty-print true
                 :externs ["resources/externs/backbone-0.9.1.js"
                           "resources/externs/underscore-0.3.1.js"]}}]}
  :main ciste.runner
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"
             ;; "-Dcom.sun.management.jmxremote"
             ;; "-Dcom.sun.management.jmxremote.port=9010"
             ;; "-Dcom.sun.management.jmxremote.local.only=false"
             ;; "-Dcom.sun.management.jmxremote.authenticate=false"
             ;; "-Dcom.sun.management.jmxremote.ssl=false"
             ]
  :repositories {"stuart"                "http://stuartsierra.com/maven2"
                 "sonatype-oss-public"   "https://oss.sonatype.org/content/groups/public/"
                 "tigase-snapshots" "http://maven.tigase.org/"
                 "apache-repo-snapshots" "https://repository.apache.org/content/repositories/snapshots"}
  :warn-on-reflection false

  :profiles {:dev
             {:resource-paths ["test-resources"]
              :dependencies
              [[midje         "1.5.1"]
               [ring-mock     "0.1.5"]]}}
  :lesscss-output-path "resources/public/assets/themes/classic/"

  :lis-opts {
             :name "jiksnu"
             }

  :plugins [[lein-cljsbuild "0.3.2"]
            [codox          "0.6.1"]
            [lein-cucumber  "1.0.2"]
            [lein-lesscss   "1.2"]
            [lein-midje     "3.0-beta1"]]
  )
