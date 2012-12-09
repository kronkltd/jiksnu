(ns jiksnu.sections.link-sections
  (:use [jiksnu.ko :only [*dynamic*]]))

(defn index-line
  [link]
  [:tr
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: href}, text: href"}
          {:href (:href link)})
     (when-not *dynamic*
       (:href link))]]
   [:td (if *dynamic*
          {:data-bind "text: (typeof($data.rel) === 'undefined') ? '' : ko.utils.unwrapObservable(rel)"}
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
