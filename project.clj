(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths   ["src"       "src-cljs"]
  :resource-paths ["resources" "target/resources"]
  :dependencies [[ciste                               "0.6.0-SNAPSHOT"
                  :exclusions [ring/ring-core
                               org.clojure/tools.reader
                               org.clojure/clojurescript
                               ;; xom
                               ]]
                 [ciste/ciste-incubator               "0.1.0-SNAPSHOT"
                  :exclusions [ciste ciste/ciste-core]]
                 [clauth                              "1.0.0-rc17"]
                 [clj-factory                         "0.2.2-SNAPSHOT"]
                 [clj-time                            "0.11.0"]
                 [clj-http                            "2.0.0"]
                 [clojurewerkz/mailer                 "1.2.0"]
                 [cheshire                            "5.5.0"
                  :exclusions [org.clojure/clojure]]
                 [compojure                           "1.4.0"]
                 [clojurewerkz/support                "1.1.0"]
                 [com.cemerick/friend                 "0.2.1"]
                 [com.flybe/socket-rocket             "0.1.9"]
                 [com.novemberain/monger              "3.0.1"]
                 [com.novemberain/validateur          "2.4.2"]
                 [com.taoensso/timbre                 "4.1.4"]
                 [crypto-random                       "1.2.0"]
                 [hiccup                              "1.0.5"]
                 [http-kit                            "2.1.19"]
                 [im.chit/gyr                         "0.3.1"]
                 [im.chit/purnam                      "0.5.2"]
                 [javax.servlet/javax.servlet-api     "3.1.0"]
                 [liberator                           "0.13"]
                 [manifold                            "0.1.1-alpha4"]
                 [mvxcvi/puget                        "0.9.2"]
                 [net.kronkltd/clj-gravatar           "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-command         "0.1.0-SNAPSHOT"]
                 [net.kronkltd/octohipster            "0.3.0-SNAPSHOT"
                  :exclusions [inflections]]
                 [net.logstash.log4j/jsonevent-layout "1.7"]
                 [org.bovinegenius/exploding-fish     "0.3.4"]
                 [org.clojure/clojure                 "1.7.0"]
                 [org.clojure/clojurescript           "1.7.28"]
                 [org.clojure/tools.logging           "0.3.1"]
                 [org.clojure/tools.reader            "0.9.2"]
                 [org.clojure/data.json               "0.2.6"]
                 [org.jsoup/jsoup                     "1.8.3"]
                 [org.slf4j/slf4j-api                 "1.7.12"]
                 [cider/cider-nrepl                   "0.10.0-SNAPSHOT"]
                 [org.slf4j/slf4j-log4j12             "1.7.12"]
                 ;; [ring "1.2.2"]
                 [ring/ring-core                      "1.4.0"]
                 [ring-basic-authentication           "1.0.5"]
                 [ring-logger-timbre                  "0.7.4"]
                 [slingshot                           "0.12.2"]
                 [timbre-logstash                     "0.2.0"]
                 ]
  :plugins [[cider/cider-nrepl "0.10.0-SNAPSHOT"]
            [codox             "0.8.13"]
            [lein-ancient      "0.6.7"]
            [lein-bikeshed     "0.2.0"]
            [lein-checkall     "0.1.1"]
            [lein-checkouts    "1.1.0"]
            [lein-cljsbuild    "1.0.6"]
            [lein-cloverage    "1.0.2"]
            [lein-less         "1.7.5"]
            [lein-midje        "3.1.3"]
            [lein-shell        "0.4.0"]
            [org.clojars.strongh/lein-init-script "1.3.1"]]
  :bower {:package-file "bower.json", :config-file ".bowerrc"}
  :aliases {"karma"             ["shell" "./node_modules/.bin/karma"             "start"]
            "protractor"        ["shell" "./node_modules/.bin/protractor"        "protractor-config.js"]
            "protractor-start"  ["shell" "./node_modules/.bin/webdriver-manager" "start"]
            "protractor-update" ["shell" "./node_modules/.bin/webdriver-manager" "update"]
            "wscat"             ["shell" "./node_modules/.bin/wscat" "-c" "ws://localhost:8080/"]}
  :main ciste.runner
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"]
  :warn-on-reflection false
  :repl-options {:init-ns ciste.runner
                 :host "0.0.0.0"
                 :port 7888}
  :appenders {:jl (make-tools-logging-appender {})}
  :cljsbuild {:repl-listen-port 9001
              :repl-launch-commands {"my-launch" ["google-chrome"]}
              :builds
              {:main {:source-paths ["src-cljs"]
                      ;; :notify-command ["notify-send"]
                      :compiler {:output-to "target/resources/public/cljs/jiksnu.js"
                                 :optimizations :simple
                                 :pretty-print true}}
               :karma      {:source-paths ["src-cljs" "test-cljs"]
                            ;; :notify-command ["notify-send"]
                            :compiler {:output-to "target/karma-test.js"
                                       :optimizations :whitespace
                                       :pretty-print true}}}}
  :profiles {:dev [:dev-core :user-dev]
             :dev-core {
                        ;; :resource-paths ["test-resources"]
                        :dependencies
                        [[midje         "1.7.0" :exclusions [org.clojure/clojure]]
                         [clj-factory "0.2.2-SNAPSHOT"]
                         [clj-webdriver "0.6.1" :exclusions [xalan]]
                         [helpshift/hydrox "0.1.2"]
                         [org.clojure/tools.nrepl "0.2.9"]
                         [ring-mock     "0.1.5"]
                         [slamhound "1.5.5"]
                         [com.palletops/log-config "0.1.4"]]}
             :production {:aot :all
                          :hooks [leiningen.cljsbuild leiningen.less]
                          :cljsbuild
                          {:builds
                           {:advanced {:source-paths ["src-cljs"]
                                       ;; :notify-command ["notify-send"]
                                       :compiler {:output-to "target/resources/public/cljs/jiksnu.min.js"
                                                  :optimizations :advanced
                                                  :pretty-print false}}}}}
             :test {:resource-paths ["target/resources"

                                     "resources"
                                     "test-resources"]

                    :cljsbuild
                     {:builds
                      {
                       :protractor {:source-paths ["specs"]
                                    ;; :notify-command ["notify-send"]
                                    :compiler {:output-to "target/protractor-tests.js"
                                               :optimizations :simple
                                               :pretty-print true}}}}}}
  :less {:source-paths ["less"]
         :target-path "target/resources/public/css"}
  :filespecs [{:type :path :path "ciste.clj"}]
  :lis-opts {:name "jiksnu"
             :properties {:ciste.properties "/vagrant/config/default.properties"}
             :jvm-opts ["-server"]})
