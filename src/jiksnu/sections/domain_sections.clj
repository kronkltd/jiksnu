(ns jiksnu.sections.domain-sections
  (:use (ciste [sections :only [defsection]] )
        (ciste.sections [default :only [link-to link-to-format]]))
  #_(:require (jiksnu.sections [link-sections :as sections.link]))
  (:import jiksnu.model.Domain)
  )

(defn add-form
  []
  [:form {:method "post" :actions "/main/domains"}
   [:fieldset
    [:legend "Add Domain"]
    [:div.clearfix
     [:label {:for "domain"} "Domain"]
     [:div.input
      [:input {:type "test" :name "domain"}]]]
    [:div.actions
     [:div.btn.primary {:type "submit" :value "Add"}]]]])


(defn delete-button
  [domain]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/discover")}
   [:input {:type "hidden" :name "_method" :value "DELETE"}]
   [:input.btn.delete-button {:type "submit" :value "Delete"}]])

(defn discover-button
  [domain]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/discover")}
   [:input.btn.discover-button {:type "submit" :value "Discover"}]])

(defn edit-button
  [domain]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/edit")}
   [:input.btn.edit-button {:type "submit" :value "Edit"}]])

(defn show-section
  [domain]
  [:div
   [:p "Id: " [:span.domain-id (:id domain)]]
   [:p "XMPP: " (:xmpp domain)]
   [:p "Discovered: " (:discovered domain)]
   #_(sections.link/index-section (:links domain))
   (discover-button domain)])

(defn index-line
  [domain]
  [:tr
   [:td
    [:img {:src (str "http://" (:_id domain) "/favicon.ico")}]]
   [:td (link-to domain)]
   [:td (:xmpp domain)]
   [:td
    [:a {:href (str "http://" (:_id domain) "/.well-known/host-meta")}
     "Host-Meta"]]
   [:td (count (:links domain))]
   [:td (discover-button domain)]
   [:td (edit-button domain)]
   [:td (delete-button domain)]])

(defn index-block
  [domains]
  [:table.domains
   [:thead
    [:tr
     [:th ]
     [:th "Name"]
     [:th "XMPP?"]
     [:th "Discovered"]
     [:th "Host Meta"]
     [:th "# Links"]
     [:th "Discover"]
     [:th "Edit"]
     [:th "Delete"]]]
   [:tbody (map index-line domains)]])

(defsection link-to [Domain :html]
  [domain & _]
  [:a {:href (str "/main/domain/" (:_id domain))}])
