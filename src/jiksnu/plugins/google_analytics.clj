(ns jiksnu.plugins.google-analytics
  (:use [ciste.config :only [describe-config]])
  (:require [jiksnu.modules.web.sections.layout-sections :as sections.layout]))

(describe-config
 [::account-id]
 String
 "Your Google Analytics account ID.")

(defn scripts-section
  [request response]
  [:script
   "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');"
   "ga('create', 'UA-93750-5', 'auto');"
   "ga('send', 'pageview');"])

(defn start
  []
  (dosync
   (alter sections.layout/scripts-section-hook conj #'scripts-section)))

(defn stop
  [])
