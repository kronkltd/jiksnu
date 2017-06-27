(defproject net.kronkltd/jiksnu "0.3.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/kronkltd/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src" "src-cljs"]
  :resource-paths ["resources" "target/resources" "node_modules"]
  :dependencies [[cider/cider-nrepl "0.14.0"]
                 [ciste "0.6.0-20170108.005400-4"
                  :exclusions [ring/ring-core
                               org.clojure/clojure
                               org.clojure/tools.reader
                               org.clojure/clojurescript
                               xerces/xercesImpl]]
                 [ciste/ciste-incubator "0.1.0-20170109.012825-2" :exclusions [ciste ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-time "0.13.0"]
                 [clj-http "3.5.0"]
                 [compojure "1.5.2"]
                 [com.cemerick/friend "0.2.3"]
                 [com.getsentry.raven/raven "8.0.2"
                  :exclusions [org.slf4j/slf4j-api]]
                 [com.novemberain/monger "3.1.0" :exclusions [com.google.guava/guava]]
                 [com.novemberain/validateur "2.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [crypto-random "1.2.0"]
                 [hiccup "1.0.5"]
                 [hiccups "0.3.0"]
                 [liberator "0.14.1"]
                 [manifold "0.1.6"]
                 [mvxcvi/puget "1.0.1"]
                 [net.kronkltd/clj-gravatar "0.1.0-20120321.005702-1"]
                 [net.kronkltd/octohipster "0.3.0-20151001.045924-2"
                  :exclusions [inflections]]
                 [org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/clojurescript "1.9.521"]
                 [org.clojure/core.async "0.3.442"
                  :exclusions [org.clojure/tools.reader]]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.reader "1.0.0-beta3"]
                 [org.clojure/data.json "0.2.6"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [ring "1.5.1"]
                 [ring/ring-defaults "0.2.3"]
                 [slingshot "0.12.2"]]
  :cljfmt {:indents {#".*fact.*" [[:inner 0]]}}
  :plugins [[codox "0.8.13" :exclusions [org.clojure/clojure]]
            [lein-ancient "0.6.10"]
            [lein-annotations "0.1.0" :exclusions [org.clojure/clojure]]
            [lein-checkall "0.1.1" :exclusions [org.clojure/tools.namespace org.clojure/clojure]]
            [lein-cljfmt "0.5.2"  :exclusions [org.clojure/clojure
                                               org.clojure/clojurescript
                                               org.clojure/tools.reader]]
            [lein-cljsbuild "1.1.4" :exclusions [org.clojure/clojure]]
            [lein-cloverage "1.0.2" :exclusions [org.clojure/clojure]]
            [lein-figwheel "0.5.8"]
            [lein-midje "3.1.3" :exclusions [org.clojure/clojure]]
            [lein-shell "0.4.0" :exclusions [org.clojure/clojure]]]
  :hiera {:ignore-ns #{"jiksnu.channels" "jiksnu.db" "jiksnu.factory" "jiksnu.mock"
                       "jiksnu.model" "jiksnu.namespace" "jiksnu.ops" "jiksnu.registry"
                       "jiksnu.session" "jiksnu.util"}}
  :aliases {"guard"            ["shell" "bundle" "exec" "guard"]
            "karma"            ["shell" "./node_modules/.bin/karma" "start"]
            "protractor"       ["shell" "./node_modules/.bin/protractor" "protractor.config.js"]
            "webdriver-start"  ["shell" "./node_modules/.bin/webdriver-manager" "start"]
            "webdriver-update" ["shell" "./node_modules/.bin/webdriver-manager" "update"]
            "wscat"            ["shell" "./node_modules/.bin/wscat" "-c" "ws://localhost/"]}
  :auto-clean false
  :jvm-opts ["-server"
             "-Dfile.encoding=UTF-8"
             "-Djava.library.path=native"
             ;; "-Dcom.sun.management.jmxremote"
             ;; "-Dcom.sun.management.jmxremote.ssl=false"
             ;; "-Dcom.sun.management.jmxremote.authenticate=false"
             ;; "-Dcom.sun.management.jmxremote.port=43210"
             ]
  :warn-on-reflection false
  :repl-options {:init-ns ciste.runner :host "0.0.0.0" :port 7888}
  :main ciste.runner
  :aot [ciste.runner]
  :cljsbuild {:builds
              {:none {:figwheel true
                      :source-paths ["src-cljs" "test-cljs"]
                      :notify-command ["notify-send"]
                      :compiler {:output-to "target/resources/public/cljs-none/jiksnu.js"
                                 :output-dir "target/resources/public/cljs-none"
                                 :optimizations :none
                                 :main "jiksnu.main"
                                 :asset-path "cljs-none"
                                 :pretty-print true}}
               :main {:source-paths ["src-cljs"]
                      :notify-command ["notify-send"]
                      :compiler {:output-to "target/resources/public/cljs/jiksnu.js"
                                 :output-dir "target/resources/public/cljs"
                                 :source-map "target/resources/public/cljs/jiksnu.js.map"
                                 ;; :main "jiksnu.app"
                                 :optimizations :simple
                                 :asset-path "cljs"
                                 ;; :verbose true
                                 :pretty-print true}}}}
  :profiles {:dev {:dependencies
                   [[midje "1.9.0-alpha5" :exclusions [org.clojure/clojure]]
                    [figwheel-sidecar "0.5.10"
                     :exclusions [http-kit org.clojure/core.cache]]
                    [org.clojure/tools.nrepl "0.2.13"]
                    [ring-mock "0.1.5"]
                    [slamhound "1.5.5"]]}
             :e2e {:cljsbuild {:builds
                               {:protractor
                                {:source-paths ["specs"]
                                 :notify-command ["notify-send"]
                                 :compiler {
                                            :output-to "target/protractor-tests.js"
                                            ;; :output-dir "target/specs/"
                                            :optimizations :simple
                                            :target :nodejs
                                            :language-in :ecmascript5
                                            :pretty-print true}}}}}
             :production {:cljsbuild {:builds
                                      {:advanced
                                       {:source-paths ["src-cljs"]
                                        :notify-command ["notify-send"]
                                        :compiler {:output-to "target/resources/public/cljs/jiksnu.min.js"
                                                   :optimizations :advanced
                                                   :pretty-print false}}}}}
             :test {:resource-paths ["target/resources" "resources" "test-resources"]
                    :cljsbuild {:builds
                                {:karma
                                 {:source-paths ["src-cljs" "test-cljs"]
                                  :notify-command ["notify-send"]
                                  :compiler {:output-to "target/karma-cljs/karma-test.js"
                                             :output-dir "target/karma-cljs"
                                             :optimizations :none
                                             ;; Fix for $q's use of 'finally'
                                             :language-in :ecmascript5
                                             :pretty-print true}}}}}}
  :filespecs [{:type :path :path "ciste.clj"}]
  ;; :repositories [["snapshots" {:url "http://repo.jiksnu.org/repository/maven-snapshots/"
  ;;                              :username [:gpg :env/repo_username]
  ;;                              :password [:gpg :env/repo_password]}]
  ;;                ["releases" {:url "http://repo.jiksnu.org/repository/maven-releases/"
  ;;                             :username [:gpg :env/repo_username]
  ;;                             :password [:gpg :env/repo_password]}]
  ;;                ["maven-mirror" {:url "http://repo.jiksnu.org/repository/maven-central/"}]]
  )
