(ns jiksnu.modules.core.sections.domain-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form index-block index-line link-to show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.session :only [current-user is-admin?]]
        [jiksnu.modules.web.sections :only [action-link admin-index-block
                                            admin-index-line bind-to
                                            control-line display-property
                                            dropdown-menu]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections.link-sections :as sections.link]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

(defn favicon-link
  [domain]
  [:img
   (if *dynamic*
     {:data-bind "attr: {src: 'http://' + _id() + '/favicon.ico'}"}
     {:src (str "http://" (:_id domain) "/favicon.ico")})])

(defn discover-button
  [item]
  (action-link "domain" "discover" (:_id item)))

(defn model-button
  [item]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/domains/' + _id() + '.model'}"}
        {:href (str (named-path "domain model" {:id (:_id item)}) ".model")})
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'discover-button])
   (when (session/is-admin?)
     [#'delete-button])))

;; actions-section

(defsection actions-section [Domain :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; add-form

(defsection add-form [Domain :html]
  [domain & _]
  [:form.well {:method "post" :actions "/main/domains"}
   [:fieldset
    [:legend "Add Domain"]
    (control-line "Domain" "domain" "text")
    [:div.actions
     [:button.btn.primary.add-button {:type "submit"}
      "Add"]]]])

;; admin-index-block

(defsection admin-index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

;; delete-button

(defsection delete-button [Domain :html]
  [item & _]
  (action-link "domain" "delete" (:_id item)))

;; index-block

(defsection index-block [Domain :html]
  [domains & _]
  [:table.domains.table
   [:thead
    [:tr
     [:th "Name"]
     [:th "HTTP"]
     [:th "HTTPS"]
     [:th "XMPP?"]
     [:th "Discovered"]
     [:th "Host Meta"]
     [:th "# Links"]
     ]]
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (map index-line domains)]])

(defsection index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; index-line

(defsection index-line [Domain :html]
  [domain & _]
  [:tr {:data-model "domain"}
   [:td
    (favicon-link domain)
    (link-to domain)]
   [:td (display-property domain :http)]
   [:td (display-property domain :https)]
   [:td (display-property domain :xmpp)]
   [:td (if *dynamic*
          {:data-bind "text: '' + !!ko.utils.unwrapObservable($data.discovered)"}
          (:discovered domain))]
   [:td
    [:a
     (if *dynamic*
       {:data-bind "attr: {href: 'http://' + _id() + '/.well-known/host-meta'}"}
       {:href (str "http://" (:_id domain) "/.well-known/host-meta")})
     "Host-Meta"]]
   [:td
    (if *dynamic*
      {:data-bind "text: links().length"}
      (count (:links domain)))]
   [:th (actions-section domain)]])

;; link-to

(defsection link-to [Domain :html]
  [domain & _]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/main/domains/' + _id()}, text: _id"}
        {:href (uri domain)})
   (when-not *dynamic*
     (:_id domain))])

;; show-section

(defsection show-section [Domain :html]
  [domain & _]
  [:div {:data-model "domain"}
   (actions-section domain)
   [:table.table
    [:thead]
    [:tbody
     [:tr
      [:th "Id"]
      [:td
       (favicon-link domain)
       [:span.domain-id (:_id domain)]]]
     [:tr
      [:th "XMPP"]
      [:td (:xmpp domain)]]
     [:tr
      [:th "Discovered"]
      [:td (:discovered domain)]]
     [:tr
      [:th "Created"]
      [:td (display-property domain :created)]]
     [:tr
      [:th "Updated"]
      [:td (display-property domain :updated)]]
     (when-let [sc (:statusnet-config domain)]
       (list
        [:tr
         [:th "Closed"]
         [:td (-> sc :site :closed)]]
        [:tr
         [:th "Private"]
         [:td (-> sc :site :private)]]
        [:tr
         [:th "Invite Only"]
         [:td (-> sc :site :inviteonly)]]
        [:tr
         [:th "Admin"]
         [:td (-> sc :site :email)]]
        (when-let [license (:license sc)]
          [:tr
           [:th "License"]
           [:td
            ;; RDFa
            [:a {:href (:url license)
                 :title (:title license)}
             [:img {:src (:image license)
                    :alt (:title license)}]]]])))]]
   (when-let [links (if *dynamic* [{}] (seq (:links domain)))]
     (bind-to "links"
       (sections.link/index-section links)))])

(defsection show-section [Domain :jrd]
  [item & [page]]
  (let [id (:_id item)
        template (format "http://%s/main/xrd?uri={uri}" id)]
    {:host id
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defsection show-section [Domain :json]
  [item & [page]]
  (with-format :jrd (show-section item page)))

(defsection show-section [Domain :model]
  [item & [page]]
  item)

(defsection show-section [Domain :viewmodel]
  [item & [page]]
  item)

(defsection show-section [Domain :xrd]
  [item & [page]]
  (let [id (:_id item)]
    ["XRD" {"xmlns" ns/xrd
            "xmlns:hm" ns/host-meta}
     ["hm:Host" id]
     ["Subject" id]
     (->> (:links item)
          (map
           ;; TODO: show-section [Link :xrd]
           (fn [{:keys [title rel href template] :as link}]
             [:Link (merge {}
                           (if rel {:rel rel})
                           (if href {:href href})
                           (if template {:template template}))
              (if title
                [:Title title])])))]))

;; uri

(defsection uri [Domain]
  [domain & _]
  (str "/main/domains/" (:_id domain)))
