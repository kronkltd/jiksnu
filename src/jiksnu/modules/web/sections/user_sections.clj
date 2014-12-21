(ns jiksnu.modules.web.sections.user-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section title uri full-uri show-section add-form
                                            edit-button delete-button link-to index-line
                                            show-section-minimal update-button index-block]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-actions-section admin-index-block
                                                 admin-index-line admin-index-section
                                                 admin-show-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-property bind-to control-line
                                                display-property dropdown-menu
                                                pagination-links]]
            [jiksnu.session :as session]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User))

(defn discover-button
  [item]
  (action-link "user" "discover" (:_id item)))

(defn subscribe-button
  [item]
  (action-link "user" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item]
  (action-link "user" "unsubscribe" (:_id item)))




(defn display-avatar-img
  [user size]
  [:img.avatar.photo
   {:width size
    :height size
    :alt ""
    :ng-src "{{user.image[0].url}}"}])

(defn display-avatar
  ([user] (display-avatar user 64))
  ([user size]
     [:a.url {:href "/remote-user/{{user.username}}@{{user.domain}}"
              :title "acct:{{user.username}}@{{user.domain}}"}
      (display-avatar-img user size)]))

(defn register-form
  [user]
  [:form.well.form-horizontal.register-form
   {:method "post" :action "/main/register"}
   [:fieldset
    [:legend "Register"]
    (map
     (fn [[label field type]]
       (control-line label field type))
     [["Username"               "username"         "text"]
      ["Password"               "password"         "password"]
      ["Confirm Password"       "confirm-password" "password"]
      ["Email"                  "email"            "email"]
      ["Display Name"           "display-name"     "text"]
      ["Location"               "location"         "text"]
      ["I have checked the box" "accepted"         "checkbox"]])
    [:div.actions
     [:input.btn.primary {:type "submit" :value "Register"}]]]])

(defn edit-form
  [user]
  [:form.well.form-horizontal {:method "post" :action "/main/profile"}
   [:fieldset
    [:legend "Edit User"]
    (control-line "Username"
                  "username" "text"
                  :value (:username user))

    (control-line "Domain"
                  "domain" "text"
                  :value (:domain user))

    (control-line "Display Name"
                  "display-name" "text"
                  :value (:name user))

    (control-line "First Name:"
                  "first-name" "text"
                  :value (:first-name user) )

    (control-line "Last Name"
                  "last-name" "text"
                  :vaue (:last-name user))

    (control-line "Email"
                  "email" "email"
                  :value (:email user))

    [:div.control-group
     [:label.control-label {:for "bio"} "Bio"]
     [:div.controls
      [:textarea {:name "bio"}
       (:bio user)]]]

    [:div.control-group
     [:label.control-label {:for "location"} "Location"]
     [:div.controls
      [:input {:type "text" :name "location" :value (:location user)}]]]

    [:div.control-group
     [:label.control-label {:for "url"} "Url"]
     [:div.controls
      [:input {:type "text" :name "url" :value (:url user)}]]]

    [:div.controls
     [:input.btn.btn-primary {:type "submit" :value "submit"}]]]])

(defn following-section
  [user]
  (let [authenticated (session/current-user)]
    (list
     (when (model.subscription/subscribing? user authenticated)
       [:p "This user follows you"])
     (when (model.subscription/subscribed? user authenticated)
       [:p "You follow this user"]))))

(defn remote-warning
  [user]
  (when-not (:local user)
    [:p "This is a cached copy of information for a user on a different system"]))

(defn links-table
  [links]
  [:div {:ng-if "user.links"}
   [:h3 "Links"]
   [:table.table
    [:thead
     [:tr
      [:th "title"]
      [:th "rel"]
      [:th "href"]
      [:th "Actions"]]]
    [:tbody
     [:tr {:ng-repeat "link in links"}
      [:td "{{link.title}}"]
      [:td "{{link.rel}}"]
      [:td "{{link.href}}"]
      [:td
       [:ul.buttons
        [:li "delete"]]]]]]])

(defn model-button
  [user]
  [:a {:href "/model/users/{{user.id}}.model"}
   "Model"])

(defn admin-button
  [user]
  [:a {:href "/admin/users/{{user.id}}"}
   "Admin"])

(defn get-buttons
  []
  (concat
   [#'subscribe-button]
   (when (session/current-user)
     [#'discover-button
      #'model-button
      #'update-button])
   (when (session/is-admin?)
     [#'edit-button
      #'admin-button
      #'delete-button])))

;; actions-section

(defsection actions-section [User :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; admin-actions-section

(defsection admin-actions-section [User :html]
  [user & [page & _]]
  (actions-section user page))

(defsection admin-index-block [User :html]
  [items & [page]]
  [:table.users.table
   [:thead
    [:tr
     [:th]
     [:th "User"]
     [:th "Id"]
     [:th "Domain"]
     [:th "Actions"]]]
   [:tbody
    (let [items [(User.)]]
      (map #(admin-index-line % page) items))]])

;; admin-index-line

(defsection admin-index-line [User :html]
  [user & [page & _]]
  [:tr {:data-model "user"
        :data-id "{{user.id}}"}
   [:td (display-avatar user)]
   [:td "{{user.username}}"]
   [:td
    [:a {:href "/admin/users/{{user.id}}"}
     "{{user.id}}"]]
   [:td
    (bind-to "domain"
      [:div {:data-model "domain"}
       (let [domain (Domain.)]
         (link-to domain))])]
   [:td (actions-section user)]])

;; admin-index-section

(defsection admin-index-section [User :html]
  [items & [page & _]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [User :html]
  [item & [response]]
  (list
   (actions-section item)
   [:table.table
    [:tr
     [:th]
     [:td (display-avatar item)]]
    [:tr
     [:th "Username"]
     [:td "{{user.username}}"]]

    [:tr
     [:th  "Domain"]
     [:td
      (bind-to "domain"
        [:div {:data-model "domain"}
         (let [domain (Domain.)]
           (link-to domain))])]]
    [:tr
     [:th "Bio"]
     [:td "{{user.bio}}"]]
    [:tr
     [:th  "Location"]
     [:td "{{user.location}}"]]
    [:tr
     [:th  "Url"]
     [:td "{{user.url}}"]]
    [:tr
     [:th  "Id"]
     [:td "{{user.id}}"]]
    [:tr
     [:th  "Discovered"]
     [:td "{{user.discovered}}"]]
    [:tr
     [:th  "Created"]
     [:td "{{user.created}}"]]
    [:tr
     [:th "Updated"]
     [:td "{{user.updated}}"]]
    [:tr
     [:th "Update Source"]
     [:td
      (bind-to "updateSource"
        (let [source (FeedSource.)]
          (link-to source)))]]]))


(defsection add-form [User :html]
  [user & _]
  [:form {:method "post" :action "/admin/users"}
   [:fieldset
    [:legend "Add User"]
    (control-line "Username" "username" "text")
    (control-line "Domain" "domain" "text")
    [:div.actions
     [:input.btn.primary {:type "submit" :value "Add User"}]]]])

(defsection delete-button [User :html]
  [user & _]
  (action-link "user" "delete" (:_id user)))

(defsection edit-button [User :html]
  [user & _]
  (action-link "user" "edit" (:_id user)))

;; index-block

(defsection index-block [User :html]
  [users & [page]]
  [:div
   [:h2 "Index Users"]
   [:table.table.users
    [:thead]
    [:tbody {:data-bag "users"}
     ;; TODO: handle this higher up
     (let [users [(User.)]]
       (map #(index-line % page) users))]]])

;; index-line

(defsection index-line [User :html]
  [user & _]
  [:tr {:ng-repeat "user in page.items"
        :data-model "user"
        :data-id "{{user.id}}"}
   [:td
    [:div
     (display-avatar user)]
    ;; TODO: call a show section here?
    [:div
     [:p (link-to user)]
     [:p "{{user.username}}@{{user.domain}}"]
     [:p "{{user.id}}"]
     [:p "{{user.displayName}}"]
     [:p "{{user.uri}}"]
     [:p "{{user.bio}}"]]]
   [:td (actions-section user)]])

(defsection link-to [User :html]
  [record & options]
  [:a {:href "/remote-user/{{user.username}}@{{user.domain}}"
       :title "acct:{{user.username}}@{{user.domain}}"}
   [:span {:property "dc:title"
           :about "{{user.url}}"}
    "{{user.displayName}}"]])

(defsection show-section-minimal [User :html]
  [user & _]
  (list
   [:div.vcard {:data-model "user"}
    ;; TODO: merge into the same link
    (display-avatar user)
    [:span.fn.n (link-to user)]]))

(defsection show-section [User :html]
  [user & options]
  (list
   [:div.vcard.user-full
    {:data-model "user"
     :data-id "{{user.id}}"}
    (actions-section user)
    [:div (display-avatar user 96)]
    [:p
     [:span.nickname.fn.n "{{user.displayName}}"]
     " ({{user.username}}@{{user.domain}})"]
    [:div.adr
     [:p.locality "{{user.location}}"]]
    [:p.note (display-property user :bio)]
    (let [source (FeedSource.)]
      (bind-to "updateSource"
        [:div {:data-model "feed-source"}
         (link-to source) ]))
    [:p [:a {:href (:id user)} (:id user)]]
    [:p [:a.url {:rel "me" :href (:url user)} (:url user)]]
    (let [key (Key.)]
      (show-section key))]))

;; update-button

(defsection update-button [User :html]
  [item & _]
  (action-link "user" "update" (:_id item)))

