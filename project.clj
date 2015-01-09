(defproject net.kronkltd/jiksnu-core "0.1.0-SNAPSHOT"
  :description "Core Library for Jiksnu"
  :url "https://github.com/duck1123/jiksnu-core"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [aleph "0.3.3"]
                 [ciste "0.6.0-SNAPSHOT"]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-stacktrace "0.2.8"]
                 [clj-time "0.9.0"]
                 [clj-http "1.0.1"]
                 [clojurewerkz/support "1.0.0"]
                 [com.novemberain/monger "1.7.0"]
                 [com.novemberain/validateur "1.7.0"]
                 [crypto-random "1.2.0"]
                 [hiccup "1.0.5"]
                 [lamina "0.5.5"]
                 [lib-noir "0.9.5"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [org.bovinegenius/exploding-fish "0.3.4"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.cache "0.6.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.reader "0.8.13"]
                 [org.clojure/data.json "0.2.5"]
                 [org.jsoup/jsoup "1.8.1"]
                 [org.slf4j/slf4j-api "1.7.9"]
                 [org.slf4j/slf4j-log4j12 "1.7.9"]
                 [slingshot "0.12.1"]
                 [xerces/xercesImpl "2.11.0"]
                 ;; I know you really want to upgrade this one, but
                 ;; you can't. :(
                 [xml-apis "1.4.01"]
]
  :profiles {:dev
             {:resource-paths ["test-resources"]
              :dependencies
              [[midje         "1.6.3"]
               [ring-mock     "0.1.5"]]}}
  :plugins [[codox          "0.8.10"]
            [lein-midje     "3.1.3"]]

  :aot [jiksnu.model]
  )
