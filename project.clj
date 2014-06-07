(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[aleph "0.3.2"]

                 [ciste "0.5.0-SNAPSHOT"]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [ciste/ciste-service-tigase "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [ciste/ciste-service-swank "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-stacktrace "0.2.8"]
                 [clj-time "0.7.0"]
                 [clj-http "0.9.2"]
                 [clojurewerkz/route-one "1.1.0"]
                 ;; [clojurewerkz/urly "1.0.0"
                 ;;  :exclusions [com.google.guava/guava]]
                 [clojurewerkz/mailer "1.0.0"]

                 [clojure-complete "0.2.3"
                  :exclusions [org.clojure/clojure]]
                 [clojurewerkz/support "0.20.0"]
                 [com.novemberain/monger "1.8.0"]
                 [com.novemberain/validateur "1.7.0"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.7"]

                 [crypto-random "1.2.0"]

                 [hiccup "1.0.5"]

                 [lamina "0.5.2"]

                 [net.kronkltd/clj-airbrake "2.0.1-SNAPSHOT"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-core "0.1.0-SNAPSHOT"
                   :exclusions [xalan]]
                 [net.kronkltd/plaza "0.3.0-SNAPSHOT"]

                 [org.apache.abdera/abdera-client "1.1.3"]
                 [org.bovinegenius/exploding-fish "0.3.4"]
                 [org.clojure/clojure "1.6.0"]

                 [org.clojure/core.cache "0.6.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.reader "0.8.3"]
                 [org.clojure/data.json "0.2.4"]
                 [org.jsoup/jsoup "1.7.3"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.7"]
                 [org.webjars/backbonejs "1.1.2-2"]
                 [org.webjars/bootstrap "3.1.1-1"]
                 [org.webjars/jquery "2.1.1"]
                 [org.webjars/knockout "3.1.0"]
                 [org.webjars/momentjs "2.6.0-2"]
                 [org.webjars/underscorejs "1.6.0-3"]
                 [ring "1.2.2"]
                 [ring/ring-core "1.1.8"]
                 [ring-basic-authentication "1.0.5"]
                 [rmarianski/tidy-up "0.0.2"]
                 [slingshot "0.10.3"]
                 [prismatic/dommy "0.1.2"]

                 [tigase/tigase-server "5.2.1"]

                 [clj-webdriver "0.6.1"
                   :exclusions [xalan]]
                 [im.chit/purnam "0.4.3"]

                 [xerces/xercesImpl "2.11.0"]

                 ;; I know you really want to upgrade this one, but
                 ;; you can't. :(
                 [xml-apis "1.4.01"]



                 [org.clojure/clojurescript "0.0-2156"]

                 ;; [lolg "0.1.0-SNAPSHOT"
                 ;;  :exclusions [org.clojure/google-closure-library]]
                 ;; [net.kronkltd/waltz "0.1.2-SNAPSHOT"
                 ;;  :exclusions [org.clojure/google-closure-library]]





                 #_[org.apache.httpcomponents/httpclient "4.2.5"]
]
  :plugins [
            [lein-cljsbuild "1.0.2"]
            ;; [lein-cljsbuild "0.3.2"]
            [codox          "0.8.8"]
            [lein-cucumber  "1.0.2"]
            [lein-lesscss   "1.2"]
            [lein-midje     "3.1.3"]]
  :cljsbuild {:repl-listen-port 9001
              :repl-launch-commands
              {"my-launch" ["google-chrome"
                            ;; "-jsconsole" "http://localhost/my-page"
                            ]}
              :builds
              [{:source-paths ["src-cljs"]
                :compiler
                {
                 :output-to "resources/public/assets/cljs/jiksnu.js"
                 :output-dir "resources/public/assets/cljs/"
                 :optimizations :whitespace
                 :pretty-print true
                 }}
               ;; {:source-paths ["src-cljs"]
               ;;  :compiler
               ;;  {:output-to "resources/public/assets/js/jiksnu.min.js"
               ;;   :optimizations :advanced
               ;;   :pretty-print true
               ;;   }}

               ]}


  ;; :exclusions [org.clojure/google-closure-library]
  ;; :aot [jiksnu.model
  ;;       ;; ciste.runner
  ;;       ;; jiksnu.modules.xmpp.plugin
  ;;       ;; jiksnu.modules.xmpp.channels
  ;;       ;; jiksnu.modules.xmpp.user-repository
  ;;       ]
  ;; :hooks [leiningen.cljsbuild]
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
              :repl-options {:init-ns ciste.runner
                             :port 7888
                             ;; :nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]
                             }
              :plugins [[com.cemerick/austin "0.1.4"]]
              :dependencies
              [[midje         "1.6.3"]
               [ring-mock     "0.1.5"]]}}
  :lesscss-output-path "resources/public/assets/themes/classic/"

  :source-paths ["src" "src-cljs"]
  :lis-opts {
             :name "jiksnu"
             }

  )
