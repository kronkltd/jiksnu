(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :dependencies [[ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-time "0.10.0"]
                 [clj-http "2.0.0"]
                 [clj-webdriver "0.6.1"
                  :exclusions [xalan]]
                 [clojurewerkz/mailer "1.2.0"]
                 ;; [clojure-complete "0.2.4"
                 ;;  :exclusions [org.clojure/clojure]]
                 [cheshire               "5.5.0" :exclusions [org.clojure/clojure]]
                 [compojure "1.4.0"]
                 [clojurewerkz/support "1.1.0"]
                 [com.novemberain/monger "3.0.0"]
                 [com.novemberain/validateur "2.4.2"]
                 [hiccup "1.0.5"]
                 [im.chit/gyr "0.3.1"]
                 [im.chit/purnam "0.5.2"]
                 [liberator "0.13"]
                 [manifold "0.1.0"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-command "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-core "0.1.0-SNAPSHOT"
                  :exclusions [xalan com.cemerick/austin]]
                 [net.kronkltd/octohipster "0.3.0-SNAPSHOT"
                  :exclusions [inflections]]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.28"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.reader "0.9.2"]
                 [org.clojure/data.json "0.2.6"]
                 [org.slf4j/slf4j-api "1.7.12"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 ;; [ring "1.2.2"]
                 [ring/ring-core "1.4.0"]
                 [ring-basic-authentication "1.0.5"]
                 [slingshot "0.12.2"]
                 [xerces/xercesImpl "2.11.0"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [codox          "0.8.12"]
            [lein-lesscss   "1.2"]
            [lein-midje     "3.1.3"]
            [cider/cider-nrepl "0.10.0-SNAPSHOT"]
            ]
  :cljsbuild {:repl-listen-port 9001
              :repl-launch-commands {"my-launch" ["google-chrome"]}
              :builds
              {:main      {:source-paths ["src-cljs"]
                           :foreign-libs [{:file "node_modules/angular/angular.min.js"
                                           :provides ["angular.core"]}]
                           :compiler {:output-to "target/resources/public/cljs/jiksnu.js"
                                      :optimizations :whitespace
                                      :pretty-print true}}
               :advanced   {:source-paths ["src-cljs"]
                            :compiler {:output-to "target/resources/public/cljs/jiksnu.min.js"
                                       :optimizations :advanced
                                       :pretty-print false}}
               :karma      {:source-paths [
                                           ;; "src-cljs"
                                           "test-cljs"
                                           ]
                            :libs ["target/resources/public/cljs/jiksnu.js"]
                            :foreign-libs [{:file "node_modules/angular/angular.min.js"
                                            :provides ["angular.core"]}]
                            :compiler {:output-to "target/karma-test.js"
                                       :optimizations :whitespace
                                       :pretty-print true}}
               :protractor {:source-paths ["specs"]
                            :compiler {:output-to "target/protractor-tests.js"
                                       :optimizations :simple
                                       :pretty-print true}}

}}
  :hooks [leiningen.cljsbuild]
  :aliases {"karma" ["shell" "./node_modules/karma-cli/bin/karma" "start"]
            "protractor" ["shell" "./node_modules/protractor/bin/protractor" "protractor-config.js"]
            "protractor-start" ["shell" "./node_modules/protractor/bin/webdriver-manager" "start"]
            "protractor-update" ["shell" "./node_modules/protractor/bin/webdriver-manager" "update"]

}
  :main ciste.runner
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"]
  :warn-on-reflection false

  :node-dependencies [
                      [angular "1.3.14"]
                      [karma "0.12.31"]
                      [karma-cli "0.0.4"]
                      [karma-chrome-launcher "0.1.7"]
                      [karma-coverage "0.2.7"]
                      [karma-growl "0.1.0"]
                      [karma-jasmine "0.1.4"]
                      [karma-junit-reporter "0.2.2"]
                      [protractor "1.8.0"]
                      ]

  :profiles {:dev [:dev-core :user-dev]
             :dev-core
             {:resource-paths ["test-resources"]
              :repl-options {:init-ns ciste.runner
                             :port 7888}
              :plugins [
                        [lein-cloverage "1.0.2"]
                        [lein-npm "0.5.0"]
                        [lein-shell "0.4.0"]
                        ]
              :dependencies
              [[midje         "1.7.0"
                :exclusions [org.clojure/clojure]]
               [ring-mock     "0.1.5"]]}}
  :lesscss-output-path "target/resources/public/css"
  :resource-paths ["resources" "target/resources"]
  :source-paths ["src" "src-cljs"]
  :lis-opts {:name "jiksnu"})
