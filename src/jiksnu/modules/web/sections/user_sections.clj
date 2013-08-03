(ns jiksnu.modules.web.sections.user-sections
  (:use  [ciste.config :only [config]]
         [ciste.sections :only [defsection]]
         [ciste.sections.default :only [actions-section title uri full-uri show-section add-form
                                        edit-button delete-button link-to index-line
                                        show-section-minimal update-button index-block]]
         [clojure.core.incubator :only [-?>]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.modules.core.sections :only [admin-actions-section
                                              admin-index-block admin-index-line admin-index-section
                                              admin-show-section]]
         [jiksnu.modules.web.sections :only [action-link bind-property bind-to control-line
                                             display-property dropdown-menu pagination-links]]
         [jiksnu.session :only [current-user is-admin?]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.form :as f]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace]
            [ring.util.codec :as codec])
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
   (merge {:width size
           :height size
           :alt ""}
          (if *dynamic*
            {:data-bind "attr: {src: avatarUrl}"}
            {:src (model.user/image-link user)}))])

(defn display-avatar
  ([user] (display-avatar user 64))
  ([user size]
     [:a.url (if *dynamic*
               {:data-bind (str "attr: {href: '/remote-user/' + username() + '@' + domain(), "
                                "title: 'acct:' + username() + '@' + domain()}")}
               {:href (full-uri user)
                :title (model.user/get-uri user)})
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
  (let [authenticated (current-user)]
    (list
     (when (model.subscription/subscribing? user authenticated)
       [:p "This user follows you"])
     (when (model.subscription/subscribed? user authenticated)
       [:p "You follow this user"]))))

(defn remote-warning
  [user]
  (when-not (:local user)
    [:p "This is a cached copy of information for a user on a different system"]))

(defn link-actions-section
  [link]
  [:ul.buttons
   [:li "delete"]])

(defn links-table
  [links]
  [:table.table
   (when *dynamic*
     {:data-bind "if: links"})
   [:thead
    [:tr
     [:th "title"]
     [:th "rel"]
     [:th "href"]
     [:th "Actions"]]]
   [:tbody
    (when *dynamic*
      {:data-bind "foreach: links"})
    (map
     (fn [link]
       [:tr
        [:td (if *dynamic* (bind-property "title") (:title link))]
        [:td (if *dynamic* (bind-property "rel")   (:rel link))]
        [:td (if *dynamic* (bind-property "href")  (:href link))]
        [:td (link-actions-section link)]])
     links)]])

(defn model-button
  [user]
  [:a (when *dynamic*
        {:data-bind "attr: {href: '/model/users/' + $data._id() + '.model'}"})
   "Model"])

(defn get-buttons
  []
  (concat
   [#'subscribe-button]
   (when (current-user)
     [#'discover-button
      #'model-button
      #'update-button])
   (when (is-admin?)
     [#'edit-button
      #'delete-button])))

;; actions-section

(defsection actions-section [User :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; admin-actions-section

(defsection admin-actions-section [User :html]
  [user & [page & _]]
  (actions-section user page))

;; admin-index-block

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
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (let [items (if *dynamic* [(User.)] items)]
      (map #(admin-index-line % page) items))]])

;; admin-index-line

(defsection admin-index-line [User :html]
  [user & [page & _]]
  [:tr (merge {:data-model "user"}
              (when-not *dynamic*
                {:data-id (:_id user)}))
   [:td (display-avatar user)]
   [:td (display-property user :username)]
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/admin/users/' + ko.utils.unwrapObservable(_id)}, text: _id"}
          {:href (format "/admin/users/%s" (:_id user))})
     (when-not *dynamic*
       (:_id user))]]
   [:td
    (bind-to "domain"
      [:div {:data-model "domain"}
       (let [domain (if *dynamic*  (Domain.) (actions.user/get-domain user))]
         (link-to domain))])]
   [:td (actions-section user)]])

;; admin-index-section

(defsection admin-index-section [User :html]
  [items & [page & _]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [User :html]
  [item & [response & _]]
  (list
   [:table.table (merge {:data-model "user"}
                        (if *dynamic*
                          {}
                          {:data-id (:_id item)}))
    [:tr
     [:th]
     [:td (display-avatar item)]]
    [:tr
     [:th "Username"]
     [:td (if *dynamic*
            {:data-bind "text: username"}
            (:username item))]]

    [:tr
     [:th  "Domain"]
     [:td
      (bind-to "domain"
        [:div {:data-model "domain"}
         (let [domain (if *dynamic* (Domain.)
                          (actions.user/get-domain item))]
           (link-to domain))])]]
    [:tr
     [:th "Bio"]
     [:td (display-property item :bio)]]
    [:tr
     [:th  "Location"]
     [:td (display-property item :location)]]
    [:tr
     [:th  "Url"]
     [:td (display-property item :url)]]
    [:tr
     [:th  "Id"]
     [:td (display-property item :id)]]
    [:tr
     [:th  "Discovered"]
     [:td (display-property item :discovered)]]
    [:tr
     [:th  "Created"]
     [:td (display-property item :created)]]
    [:tr
     [:th "Updated"]
     [:td (display-property item :updated)]]
    [:tr
     [:th "Update Source"]
     [:td
      (bind-to "updateSource"
        (when-let [source (if *dynamic*
                            (FeedSource.)
                            (-?> item :update-source model.feed-source/fetch-by-id))]
          (link-to source)))]]]
   ;; (actions-section item)
   #_(let [links (if *dynamic*  [{}] (:links item))]
     (links-table links))))


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
  [:table.table.users
   [:thead]
   [:tbody (merge {:data-bag "users"}
                  (when *dynamic* {:data-bind "foreach: items"}))
    ;; TODO: handle this higher up
    (let [users (if *dynamic* [(User.)] users)]
      (map #(index-line % page) users))]])

;; index-line

(defsection index-line [User :html]
  [user & _]
  [:tr (merge {:data-model "user"}
              (if *dynamic*
                {}
                {:data-id (:_id user)}))
   [:td
    [:div
     (display-avatar user)]
    ;; TODO: call a show section here?
    [:div
     [:p (link-to user)]
     [:p
      [:span
       (if *dynamic*
         {:data-bind "text: username"}
         (:username user))]
      "@"
      [:span
       (if *dynamic*
         {:data-bind "text: domain"}
         (:domain user))]]
     [:p (when *dynamic* {:data-bind "text: displayName"})]
     [:p
      (if *dynamic*
        {:data-bind "text: uri"}
        (:uri user))]
     [:p
      (if *dynamic*
        {:data-bind "text: bio"}
        (:bio user))]]]
   [:td (actions-section user)]])

(defsection link-to [User :html]
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a (if *dynamic*
          {:data-bind (str "attr: {href: '/remote-user/' + username() + '@' + domain(), "
                           "title: 'acct:' + username() + '@' + domain()}")}
          {:href (uri record)})
     [:span (merge {:property "dc:title"}
                   (if *dynamic*
                     {:data-bind "attr: {about: url}, text: name() || username()"}
                     {:about (uri record)}))
      (when-not *dynamic*
        (or (:title options-map) (title record)))]]))

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
    (merge {:data-model "user"}
           (if *dynamic*
             {}
             {:data-id (:_id user)}))
    (actions-section user)
    [:div (display-avatar user 96)]
    [:p
     [:span.nickname.fn.n
      (display-property user :displayName)]
     " ("
     (display-property user :username)
     "@"
     (display-property user :domain)
     ")"]
    [:div.adr
     [:p.locality (display-property user :location)]]
    [:p.note (display-property user :bio)]
    (if-let [source (if *dynamic* (FeedSource.) nil)]
      (bind-to "updateSource"
        [:div {:data-model "feed-source"}
         (link-to source) ]))
    [:p [:a {:href (:id user)} (:id user)]]
    [:p [:a.url {:rel "me" :href (:url user)} (:url user)]]
    (when (:discovered user)
      (if-let [key (if *dynamic*
                     (Key.)
                     (try+  (model.key/get-key-for-user user)
                            (catch Object ex
                              (trace/trace "errors:handled" ex)
                              nil)))]
               (show-section key)))]))

;; update-button

(defsection update-button [User :html]
  [item & _]
  (action-link "user" "update" (:_id item)))

