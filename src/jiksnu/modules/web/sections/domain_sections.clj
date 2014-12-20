(ns jiksnu.modules.core.sections.domain-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form delete-button index-block
                                            index-line link-to show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections :refer [action-link bind-to control-line display-property
                                                 dropdown-menu]]
            [jiksnu.modules.web.sections.link-sections :as sections.link]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

(defn favicon-link
  [domain]
  [:img {:src "http://{{domain.id}}/favicon.ico"}])

(defn discover-button
  [item]
  (action-link "domain" "discover" (:_id item)))

(defn model-button
  [item]
  [:a {:href "/model/domains/{{domain.id}}.model"}
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'discover-button])
   (when (session/is-admin?)
     [#'delete-button])))

(defsection actions-section [Domain :html]
  [item]
  (dropdown-menu item (get-buttons)))

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
  [item & _]
  (action-link "domain" "delete" (:_id item)))

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
   [:tbody
    (let [domain (first domains)]
      [:tr {:data-model "domain"
            :ng-repeat "domain in page.items"}
       [:td
        (favicon-link domain)
        (link-to domain)]
       [:td "{{domain.http}}"]
       [:td "{{domain.https}}"]
       [:td "{{domain.xmpp}}"]
       [:td "{{domain.discovered}}"]
       [:td
        [:a {:href "http://{{domain.id}}/.well-known/host-meta"}
         "Host-Meta"]]
       [:td "{{domain.links.length}}"]
       [:th (actions-section domain)]])]])

;; link-to

(defsection link-to [Domain :html]
  [domain & _]
  [:a {:href "/main/domains/{{domain.id}}"}
   "{{domain.id}}"])

;; show-section

(defsection show-section [Domain :html]
  [domain & _]
  (let [sc (:statusnet-config domain)
        license (:license sc)]
    [:div {:data-model "domain"}
     (actions-section domain)
     [:table.table
      [:thead]
      [:tbody
       [:tr
        [:th "Id"]
        [:td
         (favicon-link domain)
         [:span.domain-id "{{domain.id}}"]]]
       [:tr
        [:th "XMPP"]
        [:td "{{domain.xmpp}}"]]
       [:tr
        [:th "Discovered"]
        [:td "{{domain.discovered}}"]]
       [:tr
        [:th "Created"]
        [:td "{{domain.created}}"]]
       [:tr
        [:th "Updated"]
        [:td "{{domain.updated}}"]]
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
       [:tr
        [:th "License"]
        [:td
         ;; RDFa
         [:a {:href (:url license)
              :title (:title license)}
          [:img {:src (:image license)
                 :alt (:title license)}]]]]]]
     (when-let [links (if *dynamic* [{}] (seq (:links domain)))]
       (bind-to "links"
                (sections.link/index-section links)))]))
