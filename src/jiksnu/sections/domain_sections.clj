(ns jiksnu.sections.domain-sections
  (:use (ciste [debug :only [spy]]
               [sections :only [defsection]] )
        ciste.sections.default
        (jiksnu [views :only [control-line]]))
  (:require (jiksnu.sections [link-sections :as sections.link]))
  (:import jiksnu.model.Domain))

(defn favicon-link
  [domain]
  [:img {:src (str "http://" (:_id domain) "/favicon.ico")}])

(defsection add-form [Domain :html]
  [domain & _]
  [:form.well {:method "post" :actions "/main/domains"}
   [:fieldset
    [:legend "Add Domain"]
    (control-line "Domain" "domain" "text")
    [:div.actions
     [:button.btn.primary.add-button {:type "submit"}
      "Add"]]]])


(defsection delete-button [Domain :html]
  [domain & _]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain))}
   [:input {:type "hidden" :name "_method" :value "DELETE"}]
   [:button.btn.delete-button {:type "submit"}
    [:i.icon-trash] [:span.button-text "Delete"]]])

(defn discover-button
  [domain]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/discover")}
   [:button.btn.discover-button {:type "submit"}
    [:i.icon-search] [:span.button-text "Discover"]]])

(defsection edit-button [Domain :html]
  [domain & _]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/edit")}
   [:button.btn.edit-button {:type "submit"}
    [:i.icon-pencil] [:span.button-text "Edit"]]])

(defsection show-section [Domain :html]
  [domain & _]
  [:div
   [:p "Id: "
    (favicon-link domain)
    [:span.domain-id (:_id domain)]]
   [:p "XMPP: " (:xmpp domain)]
   [:p "Discovered: " (:discovered domain)]
   (when-let [sc (:statusnet-config domain)]
     (list [:p "Closed: " (-> sc :site :closed)]
           [:p "Private: " (-> sc :site :private)]
           [:p "Invite Only: " (-> sc :site :inviteonly)]
           [:p "Admin: " (-> sc :site :email)]
           (when-let [license (:license sc)]
             [:p "License: "
              ;; RDFa
              [:a {:href (:url license)
                   :title (:title license)}
               [:img {:src (:image license)
                      :alt (:title license)}]]])))
   (when (seq (:links domain))
     (sections.link/index-section (:links domain)))
   #_(discover-button domain)])

(defsection index-line [Domain :html]
  [domain & _]
  [:tr
   [:td (favicon-link domain)]
   [:td (link-to domain)]
   [:td (:xmpp domain)]
   [:td (:discovered domain)]
   [:td
    [:a {:href (str "http://" (:_id domain) "/.well-known/host-meta")}
     "Host-Meta"]]
   [:td (count (:links domain))]
   #_[:th
    (discover-button domain)
    (edit-button domain)
    (delete-button domain)]])

(defsection index-block [Domain :html]
  [domains & _]
  [:table.domains.table
   [:thead
    [:tr
     [:th ]
     [:th "Name"]
     [:th "XMPP?"]
     [:th "Discovered"]
     [:th "Host Meta"]
     [:th "# Links"]
     ;; [:th "Actions"]
     ;; [:th "Discover"]
     ;; [:th "Edit"]
     ;; [:th "Delete"]
     ]]
   [:tbody (map index-line domains)]])

(defsection link-to [Domain :html]
  [domain & _]
  [:a {:href (str "/main/domains/" (:_id domain))} (:_id domain)])
