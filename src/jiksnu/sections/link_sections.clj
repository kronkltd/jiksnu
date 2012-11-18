(ns jiksnu.sections.link-sections
  (:use [jiksnu.ko :only [*dynamic*]]))

(defn index-line
  [link]
  [:tr
   [:td (if *dynamic*
          {:data-bind "text: href"}
          (:href link))]
   [:td (if *dynamic*
          {:data-bind "text: rel"}
          (:rel link))]
   [:td (if *dynamic*
          {:data-bind "text: (typeof($data.type) === 'undefined') ? '' : ko.utils.unwrapObservable(type)"}
          (:type link))]
   #_[:td (if *dynamic*
          {:data-bind "text: template"}
          (:template link))]
   #_[:td (if *dynamic*
          {:data-bind "text: lang"}
          (:lang link))]])

(defn index-section
  [links]
  [:table.table
   [:thead
    [:tr
     [:th "Href"]
     [:th "rel"]
     [:th "Type"]
     #_[:th "template"]
     #_[:th "lang"]]]
   [:tbody (when *dynamic*
             {:data-bind "foreach: $data"})
    (map index-line links)]])
