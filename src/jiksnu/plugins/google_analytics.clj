(ns jiksnu.plugins.google-analytics
  (:use [ciste.config :only [config describe-config]]
        [ciste.initializer :only [definitializer]])
  (:require [jiksnu.sections.layout-sections :as sections.layout]))

(describe-config [::account-id]
  String
  "Your Google Analytics account ID.")

(defn scripts-section
  [request response]
  [:script {:type "text/javascript"}
   "var _gaq = _gaq || [];"
   "_gaq.push(['_setAccount', '" (config ::account-id) "']);"
   "_gaq.push(['_trackPageview']);"
   "(function() {var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true; ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js'; var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);})();"])

(defn start
  []
  (dosync
   (alter sections.layout/scripts-section-hook conj #'scripts-section)))
