(ns jiksnu.sections.domain-sections
  (:require (jiksnu.sections [link-sections :as sections.link])))

(defn discover-button
  []
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/discover")}
   [:input.btn.discover-button {:type "submit" :value "Discover"}]])

(defn show-section
  [domain]
  [:div
   [:p "Id: " [:span.domain-id (:id domain)]]
   [:p "XMPP: " (:xmpp domain)]
   [:p "Discovered: " (:discovered domain)]
   (sections.link/index-section (:links domain))
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
