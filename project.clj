(defproject net.kronkltd/jiksnu-core "0.1.0-SNAPSHOT"
  :description "Core Library for Jiksnu"
  :url "https://github.com/duck1123/jiksnu-core"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [ciste "0.6.0-SNAPSHOT"
                  :exclusions [ring/ring-core
                               org.clojure/tools.reader
                               org.clojure/clojurescript
                               ;; xom
]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-time "0.11.0"]
                 [clj-http "2.0.0"]
                 [clojurewerkz/support "1.1.0"]
                 [cheshire "5.5.0"]
                 [com.cemerick/friend "0.2.1"]
                 [com.novemberain/monger "3.0.1"]
                 [com.novemberain/validateur "2.4.2"
                  :exclusions [org.clojure/tools.reader]]
                 [crypto-random "1.2.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.1.19"]
                 [manifold "0.1.1-alpha4"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [org.bovinegenius/exploding-fish "0.3.4"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.jsoup/jsoup "1.8.3"]
                 [slingshot "0.12.2"]]
  :profiles {:dev
             {:resource-paths ["test-resources"]
              :dependencies
              [[midje         "1.7.0"]
               [ring-mock     "0.1.5"]
               [org.slf4j/slf4j-log4j12 "1.7.12"]
               [org.clojure/tools.nrepl "0.2.11"]]}}
  :plugins [[cider/cider-nrepl "0.10.0-SNAPSHOT"]
            [codox          "0.8.13"]
            [lein-midje     "3.1.3"]
            [lein-ancient "0.6.7"]
            [lein-bikeshed "0.2.0"]
            ])
