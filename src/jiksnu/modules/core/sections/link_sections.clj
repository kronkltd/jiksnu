(ns jiksnu.sections.link-sections
  (:use [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [display-property dump-data]]))

(defn index-line
  [link]
  [:tr
   [:td (display-property link :href)
    #_[:a (if *dynamic*
          {:data-bind "attr: {href: href}, text: href"}
          {:href (:href link)})
     (when-not *dynamic*
       (:href link))]]
   [:td (display-property link :rel)]
   [:td (display-property link :type)]
   [:td (display-property link :template)]
   [:td (display-property link :lang)]])

(defn index-section
  [links]
  [:table.table
   [:thead
    [:tr
     [:th "Href"]
     [:th "rel"]
     [:th "Type"]
     [:th "Template"]
     [:th "Lang"]]]
   [:tbody (when *dynamic*
             {:data-bind "foreach: $data"})
    (map index-line links)]])
