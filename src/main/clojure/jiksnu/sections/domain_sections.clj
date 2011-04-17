(ns jiksnu.sections.domain-sections
  (:use ciste.sections
        ciste.sections.default)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.Domain))

(defsection uri [Domain :html]
  [domain & options]
  (str "/domains/" (:_id domain)))

(defsection add-form [Domain :html]
  [domain & options]
  [:div
   [:p "Add Domain"]
   (f/form-to
    [:post "/domains"]
    [:p
     (f/label :domain "Domain")
     (f/text-field :domain (:_id domain))
     (f/submit-button "Add")])])

(defsection index-line [Domain :html]
  [domain & options]
  [:tr
   [:td
    [:a {:href (uri domain)} (:_id domain)]]
   [:td (:xmpp domain)]
   [:td
    [:a {:href (str "http://" (:_id domain)
                    "/.well-known/host-meta")} "Host-Meta"]]
   [:td (count (:links domain))]
   [:td [:a {:href (str "/domains/" (:_id domain) "/edit")} "Edit"]]
   [:td (f/form-to
         [:post (str "/domains/" (:_id domain) "/discover")]
         (f/submit-button "Discover"))]
   [:td (f/form-to
         [:delete (uri domain)]
         (f/submit-button "Delete"))]])

(defsection index-section [Domain :html]
  [domains & options]
  [:table
   [:tr
    [:th "Name"]
    [:th "XMPP Enabled?"]
    [:th "Host-Meta"]
    [:th "Link Count"]
    [:th "Edit"]
    [:th "Discover"]
    [:th "Delete"]
    ]
   (map index-line  domains)])

(defsection index-block [Domain :html]
  [domains & options]
  [:div
    [:p "index domains"]
   (index-section domains)
   (add-form (Domain.))])

