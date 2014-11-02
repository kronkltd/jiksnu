(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[aleph "0.3.3"]
                 [ciste "0.5.0-SNAPSHOT"]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-time "0.8.0"]
                 [clj-http "1.0.1"]
                 [clj-webdriver "0.6.1"
                  :exclusions [xalan]]
                 [clojurewerkz/route-one "1.1.0"]
                 [clojurewerkz/mailer "1.2.0"]
                 [clojure-complete "0.2.4"
                  :exclusions [org.clojure/clojure]]
                 [clojurewerkz/support "1.1.0"]
                 [com.novemberain/monger "1.8.0"]
                 [com.novemberain/validateur "1.7.0"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.7"]
                 [crypto-random "1.2.0"]
                 [hiccup "1.0.5"]
                 [im.chit/purnam "0.5.1"]
                 [lamina "0.5.5"]
                 [lolg "0.1.0-SNAPSHOT"
                  :exclusions [org.clojure/google-closure-library]]
                 [net.kronkltd/clj-airbrake "2.0.1-SNAPSHOT"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-core "0.1.0-SNAPSHOT"
                  :exclusions [xalan]]
                 [net.kronkltd/plaza "0.3.0-SNAPSHOT"]
                 [onlyafly/waltz "0.1.2"]
                 [org.apache.abdera/abdera-client "1.1.3"]
                 [org.bovinegenius/exploding-fish "0.3.4"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.cache "0.6.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.reader "0.8.11"]
                 [org.clojure/data.json "0.2.5"]
                 [org.jsoup/jsoup "1.8.1"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.7"]
                 [org.webjars/angularjs "1.3.0"]
                 [org.webjars/backbonejs "1.1.2-2"]
                 [org.webjars/bootstrap "3.3.0"]
                 [org.webjars/jquery "2.1.1"]
                 [org.webjars/knockout "3.2.0"]
                 [org.webjars/momentjs "2.8.3"]
                 [org.webjars/underscorejs "1.7.0"]
                 [prismatic/dommy "1.0.0"]
                 [ring "1.2.2"]
                 [ring/ring-core "1.1.8"]
                 [ring-basic-authentication "1.0.5"]
                 [rmarianski/tidy-up "0.0.2"]
                 [slingshot "0.12.1"]
                 [xerces/xercesImpl "2.11.0"]
                 ;; I know you really want to upgrade this one, but
                 ;; you can't. :(
                 [xml-apis "1.4.01"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [codox          "0.8.10"]
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
                 :output-to "resources/public/cljs/jiksnu.js"
                 :output-dir "resources/public/cljs/"
                 :optimizations :whitespace
                 :pretty-print true
                 }}
               ]}
  :hooks [leiningen.cljsbuild]
  :main ciste.runner
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"]
  :warn-on-reflection false

  :profiles {:dev
             {:resource-paths ["test-resources"]
              :repl-options {:init-ns ciste.runner
                             :port 7888}
              :plugins [[com.cemerick/austin "0.1.4"]]
              :dependencies
              [[midje         "1.6.3"]
               [ring-mock     "0.1.5"]]}}
  :lesscss-output-path "resources/public/css"

  :source-paths ["src" "src-cljs"]
  :lis-opts {
             :name "jiksnu"
             }

  )
