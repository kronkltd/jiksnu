(ns jiksnu.sections.link-sections)

(defn index-line
  [link]
  [:tr
   [:td (:href link)]
   [:td (:rel link)]
   [:td (:template link)]
   [:td (:lang link)]])

(defn index-section
  [links]
  [:table.table
   [:thead
    [:tr
     [:th "Href"]
     [:th "rel"]
     [:th "template"]
     [:th "lang"]]]
   [:tbody
    (map index-line links)]])
