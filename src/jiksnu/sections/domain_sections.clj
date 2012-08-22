(ns jiksnu.sections.domain-sections
  (:use [ciste.sections :only [defsection]]
        ciste.sections.default
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.session :only [current-user is-admin?]]
        [jiksnu.sections :only [admin-index-block admin-index-line
                                control-line]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.sections.link-sections :as sections.link])
  (:import jiksnu.model.Domain))

(defn favicon-link
  [domain]
  [:img
   (if *dynamic*
     {:data-bind "attr: {src: 'http://' + _id + '/favicon.ico'}"}
     {:src (str "http://" (:_id domain) "/favicon.ico")})])

(defsection uri [Domain]
  [domain & _]
  (str "/main/domains/" (:_id domain)))

(defn discover-button
  [domain]
  [:form {:method "post"
          :action (str "/main/domains/" (:_id domain) "/discover")}
   [:button.btn.discover-button {:type "submit"}
    [:i.icon-search] [:span.button-text "Discover"]]])

(defsection actions-section [Domain :html]
  [domain & _]
  [:ul
   [:li (discover-button domain)]
   [:li (delete-button domain)]
   ]
  )

(defsection add-form [Domain :html]
  [domain & _]
  [:form.well {:method "post" :actions "/main/domains"}
   [:fieldset
    [:legend "Add Domain"]
    (control-line "Domain" "domain" "text")
    [:div.actions
     [:button.btn.primary.add-button {:type "submit"}
      "Add"]]]])

(defsection admin-index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

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
     [:th "Actions"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: $data"})
    (map index-line domains)]])

(defsection index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection index-line [Domain :html]
  [domain & _]
  [:tr
   [:td (favicon-link domain)]
   [:td (link-to domain)]
   [:td (if *dynamic*
          {:data-bind "text: xmpp"}
          (:xmpp domain))]
   [:td (if *dynamic*
          {:data-bind "text: typeof(discovered) !== 'undefined' ? discovered : ''"}
          (:discovered domain))]
   [:td
    [:a
     (if *dynamic*
       {:data-bind "attr: {href: 'http://' + _id + '/.well-known/host-meta'}"}
       {:href (str "http://" (:_id domain) "/.well-known/host-meta")})
     "Host-Meta"]]
   [:td
    (if *dynamic*
      {:data-bind "text: typeof(links) !== 'undefined' ? links.length : 0"}
      (count (:links domain)))]
   [:th (actions-section domain)]])

(defsection link-to [Domain :html]
  [domain & _]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/main/domains/' + _id}, text: _id"}
        {:href (uri domain)})
   (when-not *dynamic*
     (:_id domain))])


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
   (when (is-admin?)
     [:ul.domain-actions.buttons
      [:li (discover-button domain)]])
   (when (seq (:links domain))
     (sections.link/index-section (:links domain)))
   (when (current-user) (discover-button domain))])

(defsection show-section [Domain :viewmodel]
  [item & [page]]
  item)

