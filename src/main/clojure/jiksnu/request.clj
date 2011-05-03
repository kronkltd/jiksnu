(ns jiksnu.request
  (:import org.jsoup.Jsoup
           java.net.URL))

(defn parse-page
  [url]
  (Jsoup/parse (URL. url) 60000)
  )

(defn find-atom-link
  [document]
  (.attr
   (.select
    document "link[rel=alternate][type=application/atom+xml]")
   "href"))
