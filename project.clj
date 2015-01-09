(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[aleph "0.3.3"]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste ciste/ciste-core]]
                 [ciste/ciste-service-aleph "0.4.0-SNAPSHOT"
                  :exclusions [ciste ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-time "0.9.0"]
                 [clj-http "1.0.1"]
                 [clj-webdriver "0.6.1"
                  :exclusions [xalan]]
                 [clojurewerkz/mailer "1.2.0"]
                 [clojure-complete "0.2.4"
                  :exclusions [org.clojure/clojure]]
                 [clojurewerkz/support "1.1.0"]
                 [com.novemberain/monger "1.8.0"]
                 [com.novemberain/validateur "1.7.0"]
                 [hiccup "1.0.5"]
                 [im.chit/gyr "0.4.0"]
                 [im.chit/purnam "0.5.1"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-core "0.1.0-SNAPSHOT"
                  :exclusions [xalan]]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.reader "0.8.11"]
                 [org.clojure/data.json "0.2.5"]
                 [org.slf4j/slf4j-api "1.7.10"]
                 [org.slf4j/slf4j-log4j12 "1.7.10"]
                 [org.webjars/angularjs "1.3.8"]
                 [org.webjars/angularjs-geolocation "0.1.1-SNAPSHOT"]
                 [org.webjars/angular-cache "3.0.2"]
                 [org.webjars/angular-file-upload "2.0.5"]
                 [org.webjars/angular-google-maps "2.0.11"]
                 [org.webjars/angular-hotkeys "1.4.0"]
                 [org.webjars/angular-markdown-directive "0.3.0"]
                 [org.webjars/angular-moment "0.8.2-1"]
                 [org.webjars/angular-notify "2.0.2"]
                 [org.webjars/angular-ui "0.4.0-3"]
                 [org.webjars/angular-ui-bootstrap "0.12.0"]
                 [org.webjars/angular-ui-router "0.2.13"]
                 [org.webjars/angular-validator "0.2.5"]
                 [org.webjars/angular-ws "1.1.0-SNAPSHOT"]
                 [org.webjars/bootstrap "3.3.1"]
                 [org.webjars/jquery "2.1.3"]
                 [org.webjars/momentjs "2.8.3"]
                 [org.webjars/underscorejs "1.7.0-1"]
                 [ring-basic-authentication "1.0.5"]
                 [slingshot "0.12.1"]
                 [xerces/xercesImpl "2.11.0"]]
  :plugins [[lein-cljsbuild "1.0.4"]
            [codox          "0.8.10"]
            ;; [lein-cucumber  "1.0.2"]
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
                {:output-to "resources/public/cljs/jiksnu.js"
                 ;; :output-dir "resources/public/cljs/"
                 :optimizations :whitespace
                 :pretty-print true}}]}
  ;; :hooks [leiningen.cljsbuild]
  :main ciste.runner
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"]
  :warn-on-reflection false

  :profiles {:dev
             {:resource-paths ["test-resources"]
              :repl-options {:init-ns ciste.runner
                             :port 7888}
              :plugins [
                        ;; [com.cemerick/austin "0.1.4"]
                        ]
              :dependencies
              [[midje         "1.7.0-SNAPSHOT"
                :exclusions [org.clojure/clojure]]
               [ring-mock     "0.1.5"]]}}
  :lesscss-output-path "resources/public/css"

  :source-paths ["src" "src-cljs"]
  :lis-opts {:name "jiksnu"})
