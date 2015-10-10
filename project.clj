(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :source-paths   ["src"       "src-cljs"]
  :resource-paths ["resources" "target/resources"]
  :dependencies [[ciste                           "0.6.0-SNAPSHOT"]
                 [ciste/ciste-incubator           "0.1.0-SNAPSHOT"
                  :exclusions [ciste ciste/ciste-core]]
                 [clj-time                        "0.10.0"]
                 [clj-http                        "2.0.0"]
                 [clojurewerkz/mailer             "1.2.0"]
                 [cheshire                        "5.5.0"
                  :exclusions [org.clojure/clojure]]
                 [compojure                       "1.4.0"]
                 [clojurewerkz/support            "1.1.0"]
                 [com.novemberain/monger          "3.0.0"]
                 [com.novemberain/validateur      "2.4.2"]
                 [hiccup                          "1.0.5"]
                 [http-kit                        "2.1.19"]
                 [im.chit/gyr                     "0.3.1"]
                 [im.chit/purnam                  "0.5.2"]
                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [liberator                       "0.13"]
                 [manifold                        "0.1.0"]
                 [net.kronkltd/clj-gravatar       "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-command     "0.1.0-SNAPSHOT"]
                 [net.kronkltd/jiksnu-core        "0.1.0-SNAPSHOT"
                  :exclusions [xalan com.cemerick/austin]]
                 [net.kronkltd/octohipster        "0.3.0-SNAPSHOT"
                  :exclusions [inflections]]
                 [org.clojure/clojure             "1.7.0"]
                 [org.clojure/clojurescript       "1.7.28"]
                 [org.clojure/tools.logging       "0.3.1"]
                 [org.clojure/tools.reader        "0.9.2"]
                 [org.clojure/data.json           "0.2.6"]
                 [org.slf4j/slf4j-api             "1.7.12"]
                 [cider/cider-nrepl               "0.10.0-SNAPSHOT"]
                 [org.slf4j/slf4j-log4j12         "1.7.12"]
                 ;; [ring "1.2.2"]
                 [ring/ring-core                  "1.4.0"]
                 [ring-basic-authentication       "1.0.5"]
                 [slingshot                       "0.12.2"]]
  :plugins [[cider/cider-nrepl "0.10.0-SNAPSHOT"]
            [codox             "0.8.12"]
            [lein-checkouts    "1.1.0"]
            [lein-cljsbuild    "1.0.6"]
            [lein-cloverage    "1.0.2"]
            [lein-less         "1.7.5"]
            [lein-midje        "3.1.3"]
            [lein-npm          "0.6.1"]
            [lein-shell        "0.4.0"]
            [org.clojars.strongh/lein-init-script "1.3.1"]]
  :cljsbuild {:repl-listen-port 9001
              :repl-launch-commands {"my-launch" ["google-chrome"]}
              :builds
              {:main {:source-paths ["src-cljs"]
                      ;; :notify-command ["notify-send"]
                      :compiler {:output-to "target/resources/public/cljs/jiksnu.js"
                                 :optimizations :simple
                                 :pretty-print true}}}}
  :hooks [leiningen.cljsbuild leiningen.less]
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

  :profiles {:dev [:dev-core :user-dev]
             :dev-core {:resource-paths ["test-resources"]
                        :dependencies
                        [[midje         "1.7.0" :exclusions [org.clojure/clojure]]
                         [clj-factory "0.2.2-SNAPSHOT"]
                         [clj-webdriver "0.6.1" :exclusions [xalan]]
                         [slamhound "1.5.5"]
                         [org.clojure/tools.nrepl "0.2.9"]
                         [ring-mock     "0.1.5"]]}
             :production {:aot :all
                          :cljsbuild
                          {:builds
                           {:advanced {:source-paths ["src-cljs"]
                                       ;; :notify-command ["notify-send"]
                                       :compiler {:output-to "target/resources/public/cljs/jiksnu.min.js"
                                                  :optimizations :advanced
                                                  :pretty-print false}}}}}
             :karma {:cljsbuild
                     {:builds
                      {:karma      {:source-paths ["src-cljs" "test-cljs"]
                                    ;; :notify-command ["notify-send"]
                                    :foreign-libs [{:file "node_modules/angular/angular.min.js"
                                                    :provides ["angular.core"]}]
                                    :compiler {:output-to "target/karma-test.js"
                                               :optimizations :whitespace
                                               :pretty-print true}}
                       :protractor {:source-paths ["specs"]
                                                       ;; :notify-command ["notify-send"]
                                    :compiler {:output-to "target/protractor-tests.js"
                                               :optimizations :simple
                                               :pretty-print true}}}}}}
  :less {:source-paths ["less"]
         :target-path "target/resources/public/css"}
  :npm {:dependencies [[angular                    "1.4.7"]
                       [karma-coverage             "0.5.2"]
                       [karma-jasmine              "0.3.6"]
                       [karma-junit-reporter       "0.3.7"]
                       [karma-notify-send-reporter "0.0.3"]
                       [karma-phantomjs-launcher   "0.2.1"]
                       [wscat                      "1.0.1"]]}
  :filespecs [{:type :path :path "ciste.clj"}]
  :lis-opts {:name "jiksnu"
             :properties {:ciste.properties "/vagrant/config/default.properties"}
             :jvm-opts ["-server"]})
