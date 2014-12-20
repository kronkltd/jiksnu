(ns jiksnu.modules.web.sections.link-sections)

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
   [:tbody
    [:tr {:ng-repeat "link in links"}
     [:td
      [:a {:href "{{link.href}}"}
       "{{link.href}}"]]
     [:td "{{link.rel}}"]
     [:td "{{link.type}}"]
     [:td "{{link.template}}"]
     [:td "{{link.lang}}"]]]])
