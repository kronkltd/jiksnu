(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "http://github.com/duck1123/jiksnu"
  :repositories {"jiksnu-internal" "http://build.jiksnu.com/repository/internal"
                 "jiksnu-snapshots" "http://build.jiksnu.com/repository/snapshots"
                 "any23-repository-external" "http://any23.googlecode.com/svn/repo-ext"
                 "any23-repository" "http://any23.googlecode.com/svn/repo"
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [
                 [clj-factory "0.1.0"]
                 [clj-tigase "0.1.0-SNAPSHOT"]
                 [clj-time "0.3.1"]
                 [clj-webdriver "0.2.14"]
                 [clojure-saxon "0.9.2"]
                 [closure-templates-clj "0.0.3"]
                 [ciste "0.2.0-SNAPSHOT"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [compojure "0.6.5"]
                 [hiccup "0.3.6"]
                 [inflections "0.5.3-SNAPSHOT"]
                 [aleph "0.2.1-SNAPSHOT"]
                 [net.kronkltd/clj-gravatar "0.0.1"]
                 [lamina "0.4.1-SNAPSHOT"]
                 [net.kronkltd/karras "0.7.1-SNAPSHOT"]
                 [midje "1.3-alpha4"]
                 [net.kronkltd/plaza-core "0.0.6-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-ext "2.0-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.match "0.2.0-alpha5"]
                 [org.clojure/data.zip "0.1.1-SNAPSHOT"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.deri.any23/any23-core "0.5.0"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/slf4j-simple "1.6.1"]
                 [pinot "0.1.1-SNAPSHOT"]
                 [ring/ring-core "0.3.11"]
                 [ring/ring-devel "0.3.11"]
                 [ring-mock "0.1.1"]
                 [swank-clojure "1.4.0-SNAPSHOT"]
                 ]
  :dev-dependencies [[org.clojars.rferraz/lein-cuke "1.1.1"]
                     [lein-marginalia "0.6.1"]]
  :exclusions [org.apache.abdera/abdera-core
               org.clojure/contrib
               org.clojure/clojure-contrib
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-jdk14]
  :aot [jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository]
  :main jiksnu.core
  :warn-on-reflection false
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"])
