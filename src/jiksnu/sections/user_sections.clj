(ns jiksnu.sections.user-sections
  (:use  [ciste.config :only [config]]
         [ciste.sections :only [defsection]]
         [ciste.sections.default :only [title uri full-uri show-section add-form
                                        edit-button delete-button link-to index-line
                                        show-section-minimal update-button index-block index-section]]
         [clojure.core.incubator :only [-?>]]
         [inflections.core :only [camelize]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.model :only [with-subject]]
         [jiksnu.sections :only [admin-actions-section
                                 admin-index-block
                                 admin-index-line
                                 admin-index-section
                                 admin-show-section control-line
                                 bind-property
                                 pagination-links]]
         [jiksnu.session :only [current-user is-admin?]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.form :as f]
            [jiksnu.abdera :as abdera]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [plaza.rdf.core :as rdf]
            [ring.util.codec :as codec])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.User
           org.apache.abdera2.model.Entry))

(defn user-timeline-link
  [user format]
  (str "http://" (config :domain)
       "/api/statuses/user_timeline/" (:_id user) "." format))

(defn discover-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/discover")}
   [:button.btn.discover-button {:type "submit" :title "Discover"}
    [:i.icon-search] [:span.button-text "Discover"]]])

(defn subscribe-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/subscribe")}
   [:button.btn.subscribe-button {:type "submit" :title "Subscribe"}
    [:i.icon-eye-open] [:span.button-text "Subscribe"]]])

(defn unsubscribe-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/unsubscribe")}
   [:button.btn.unsubscribe-button {:type "submit" :title "Unsubscribe"}
    [:i.icon-eye-close] [:span.button-text "Unsubscribe"]]])


;; (defn add-author
;;   "Adds the supplied user to the atom entry"
;;   [^Entry entry ^User user]
;;   ;; TODO: Do we need to re-fetch here?
;;   (if-let [user (model.user/fetch-by-id (:_id user))]
;;     (let [name (:name user)
;;           jid  (model.user/get-uri user false)
;;           actor (.addExtension entry ns/as "actor" "activity")]
;;       (doto actor
;;         (.addSimpleExtension ns/atom "name" "" name)
;;         (.addSimpleExtension ns/atom "email" "" jid)
;;         (.addSimpleExtension ns/atom "uri" "" jid))
;;       (doto entry
;;         (.addExtension actor)
;;         (.addExtension (show-section user))))))

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
  ([user] (display-avatar user 48))
  ([user size]
     [:a.url (if *dynamic*
               {:data-bind "attr: {href: \"/users/\" + _id, title: name}"}
               {:href (full-uri user)
                :title (:name user)})
      (display-avatar-img user size)]))

(defn register-form
  [user]
  [:form.well.form-horizontal.register-form {:method "post" :action "/main/register"}
   [:fieldset
    [:legend "Register"]

    (control-line "Username" "username" "text")
    (control-line "Password" "password" "password")
    (control-line "Confirm Password" "confirm-password" "password")
    (control-line "Email" "email" "email")
    (control-line "Display Name" "display-name" "text")
    (control-line "Location" "location" "text")
    (control-line "I have checked the box" "accepted" "checkbox")

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
                  :value (:display-name user))
    
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
     [:input.btn.btn-primary {:type "submit" :value "submit"}]]]]
  
  )

(defn following-section
  [user]
  (let [authenticated (current-user)]
    (list
     (when (model.subscription/subscribing? user authenticated)
       [:p "This user follows you"])
     (when (model.subscription/subscribed? user authenticated)
       [:p "You follow this user"]))))

(defn user->person
  [user]
  (let [author-uri (model.user/get-uri user)]
    (let [person (.newAuthor abdera/abdera-factory)]
      (doto person
       (.addSimpleExtension ns/as "object-type" "activity" ns/person)
       (.addSimpleExtension ns/atom "id" "" (or (:id user)
                                                author-uri))
       (.setName (or (:name user)
                     (:display-name user)
                     (str (:first-name user) " " (:last-name user))))
      
       (.addExtension (doto (.newLink abdera/abdera-factory)
                        (.setHref (:avatar-url user))
                        (.setRel "avatar")
                        (.setMimeType "image/jpeg")))
       
       (.addSimpleExtension ns/atom "uri" "" author-uri)

       (.addSimpleExtension ns/poco "preferredUsername" "poco" (:username user))
       (.addSimpleExtension ns/poco "displayName" "poco" (title user))
       (.addExtension (doto (.newLink abdera/abdera-factory)
                        (.setHref (full-uri user))
                        (.setRel "alternate")
                        (.setMimeType "text/html")))
       (-> (.addExtension ns/status "profile_info" "statusnet")
           (.setAttributeValue "local_id" (str (:_id user))))
       (-> (.addExtension ns/poco "urls" "poco")
           (doto (.addSimpleExtension ns/poco "type" "poco" "homepage")
             (.addSimpleExtension ns/poco "value" "poco" (full-uri user))
             (.addSimpleExtension ns/poco "primary" "poco" "true"))))

      (when (:email user)
        (.addSimpleExtension person ns/atom "email" "" (:email user)))
      person)))

(defn user-actions
  [user]
  (if-let [authenticated (current-user)]
    [:div
     (when (= (:_id user) (:_id authenticated))
       [:p "This is you"])
     [:ul.user-actions.buttons
      [:li (discover-button user)]
      [:li (update-button user)]
      (when (not= (:_id user) (:_id authenticated))
        (list
         [:li (subscribe-button user)]
         [:li (unsubscribe-button user)]))
      (when (is-admin?)
        [:li (delete-button user)])]]))

(defn push-subscribe-button
  [user]
  [:a.url {:href (:url user) :rel "contact"}
   [:span.fn.n (:display-name user)]])

(defn remote-warning
  [user]
  (when-not (:local user)
    [:p "This is a cached copy of information for a user on a different system"]))

(defn pagination-links
  [options]
  ;; TODO: page should always be there from now on
  (let [page-number (get options :page 1)
        page-size (get options :page-size 20)
        ;; If no total, no pagination
        total (get options :total-records 0)]
    [:ul.pager
     (when (> page-number 1)
       [:li.previous [:a {:href (str "?page=" (dec page-number)) :rel "prev"} "&larr; Previous"]])
     (when (< (* page-number page-size) total)
       [:li.next [:a {:href (str "?page=" (inc page-number)) :rel "next"} "Next &rarr;"]])]))


(defn link-actions-section
  [link]
  [:ul.buttons
   [:li "delete"]])

(defn actions-section
  [user]
  [:ul.buttons
   [:li (subscribe-button user)]
   (when (current-user)
     (list
      [:li (discover-button user)]
      [:li (update-button user)]
      (when (is-admin?)
        (list
         [:li (edit-button user)]
         [:li (delete-button user)]))))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sections
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; admin-actions-section

(defsection admin-actions-section [User :html]
  [user & [page & _]]
  [:ul.user-actions.buttons
   [:li (discover-button user)]
   [:li (update-button user)]
   [:li (edit-button user)]
   [:li (delete-button user)]])

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
   [:tbody (when *dynamic* {:data-bind "foreach: $data"})
    (let [items (if *dynamic* [(User.)] items)]
      (map #(admin-index-line % page) items))]])

(defsection admin-index-block [User :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

;; admin-index-line

(defsection admin-index-line [User :html]
  [user & [page & _]]
  [:tr (merge {:data-type "user"}
              (if *dynamic*
                {:data-bind "attr: {'data-id': _id}"}
                {:data-id (:_id user)}))
   [:td (display-avatar user)]
   [:td
    (if *dynamic*
      {:data-bind "text: username"}
      (link-to user))]
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/admin/users/' + _id}, text: _id"}
          {:href (format "/admin/users/%s" (:_id user))})
     (when-not *dynamic*
       (:_id user))]]
   [:td {:data-bind "with: $root.getDomain($data.domain)"}
    (let [domain (if *dynamic*  (Domain.) (actions.user/get-domain user))]
      (link-to domain))]
   [:td (admin-actions-section user page)]])

;; admin-index-section

(defsection admin-index-section [User :html]
  [items & [page & _]]
  (admin-index-block items page))

(defsection admin-index-section [User :viewmodel]
  [items & [page]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [User :html]
  [item & [response & _]]
  (list
   [:table.table (merge {:data-type "user"}
                (if *dynamic*
                  {:data-bind "attr: {'data-id': _id}"}
                  {:data-id (:_id item)}))
    [:tr
     [:th]
     [:td
      (display-avatar item)]]
    [:tr
     [:th "Username"]
     [:td (if *dynamic*
            {:data-bind "text: username"}
            (:username item))]]
    
    [:tr
     [:th  "Domain"]
     [:td (when *dynamic*
              {:data-bind "with: $root.getDomain($data.domain)"})
      (let [domain (if *dynamic* (Domain.)
                       (actions.user/get-domain item))]
        (link-to domain))]]
    [:tr
     [:th "Bio"]
     [:td (if *dynamic*
              (bind-property "bio")
              (:bio item))]]
    [:tr
     [:th  "Location"]
     [:td (if *dynamic*
              (bind-property "location")
              (:location item))]]
    [:tr
     [:th  "Url"]
     [:td (if *dynamic*
              (bind-property "url")
              (:url item))]]
    [:tr
     [:th  "Id"]
     [:td (if *dynamic*
              (bind-property "id")
              (:id item))]]
    [:tr
     [:th  "Discovered"]
     [:td (if *dynamic*
              (bind-property "discovered")
              (:discovered item))]]
    [:tr
     [:th  "Created"]
     [:td (if *dynamic*
              (bind-property "created")
              (:created item))]]
    [:tr
     [:th "Updated"]
     [:td (if *dynamic*
              {:data-bind "text: updated"}
              (:updated item))]]
    [:tr
     [:th "Update Source"]
     [:td (if *dynamic*
            {:data-bind "with: $root.getFeedSource($data.updateSource)"})
      (when-let [source (if *dynamic*
                          (FeedSource.)
                          (-?> item :update-source model.feed-source/fetch-by-id))]
        (link-to source))]]]
   (admin-actions-section item)
   [:table.table (when *dynamic* {:data-bind "if: typeof(links) !== 'undefined'"})
    [:thead
     [:tr
      [:th "title"]
      [:th "rel"]
      [:th "href"]
      [:th "Actions"]]]
    [:tbody (when *dynamic* {:data-bind "foreach: typeof(links) !== 'undefined' ? links : []"})
     (map
      (fn [link]
        [:tr
         [:td (if *dynamic* (bind-property "title") (:title link))]
         [:td (if *dynamic* (bind-property "rel") (:rel link))]
         [:td (if *dynamic* (bind-property "href") (:href link))]
         [:td (link-actions-section link)]])
      (if *dynamic*
        [{}]
        (:links item)))]]
    ))


(defsection title [User]
  [user & options]
  (or (:name user)
      (:display-name user)
      (:first-name user)
      (model.user/get-uri user)))


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
  [:form {:method "post" :action (str "/users/" (:_id user) "/delete")}
   [:button.btn.delete-button {:type "submit" :title "Delete"}
    [:i.icon-trash] [:span.button-text "Delete"]]])

(defsection edit-button [User :html]
  [user & _]
  [:form {:method "post" :action (str "/users/" (:_id user) "/edit")}
   [:button.btn.edit-button {:type "submit" :title "Edit"}
    [:i.icon-pencil] [:span.button-text "Edit"]]])

;; index-block

(defsection index-block [User :html]
  [users & [page]]
  [:table.table.users
   [:thead]
   [:tbody (merge {:data-bag "users"}
                  (when *dynamic* {:data-bind "foreach: $data"}))
    ;; TODO: handle this higher up
    (let [users (if *dynamic* [(User.)] users)]
      (map #(index-line % page) users))]])

(defsection index-block [User :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (index-line m page)}))
       (into {})))

;; index-line

(defsection index-line [User :html]
  [user & _]
  [:tr (merge {:data-type "user"}
              (if *dynamic*
                {:data-bind "attr: {'data-id': _id}"}
                {:data-id (:_id user)}))
   [:td (display-avatar user)]
   [:td
    ;; TODO: call a show section here?
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
    [:p (when *dynamic* {:data-bind "text: typeof($data.displayName) != 'undefined' ? displayName : ''"})]
    [:p
     (if *dynamic*
       {:data-bind "text: typeof($data.uri) != 'undefined' ? uri : ''"}
       (:uri user))]
    [:p
     (if *dynamic*
       {:data-bind "text: typeof($data.bio) !== 'undefined' ? bio : ''"}
       (:bio user))]]
   [:td (actions-section user)]])

(defsection index-line [User :viewmodel]
  [item & page]
  (show-section item page))

(defsection index-section [User :viewmodel]
  [items & [page]]
  (index-block items page))

(defsection link-to [User :html]
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/users/' + _id}"}
          {:href (uri record)})
     [:span (merge {:property "dc:title"}
                   (if *dynamic*
                     {:data-bind "attr: {about: typeof($data.url) !== 'undefined' ? url : ''}, text: typeof(displayName) !== 'undefined' ? displayName : ''"}
                     {:about (uri record)}))
      (when-not *dynamic*
       (or (:title options-map) (title record)))] ]))

(defsection show-section-minimal [User :html]
  [user & _]
  [:div.vcard
   ;; TODO: merge into the same link
   (display-avatar user)
   [:span.fn.n (link-to user)]])

;; show-section

(defsection show-section [User :as]
  [user & options]
  (let [{:keys [display-name id avatar-url]} user
        avatar-url (or avatar-url (model.user/image-link user))]
    (merge {:profileUrl (full-uri user)
            :id (or id (model.user/get-uri user))
            :url (or (:url user)
                     (full-uri user))
            :objectType "person"
            :username (:username user)
            :domain (:domain user)
            :published (:updated user)
            ;; :name {:formatted (:display-name user)
            ;;        :familyName (:last-name user)
            ;;        :givenName (:first-name user)}
            }
           (when avatar-url
             ;; TODO: get image dimensions
             {:image [{:url avatar-url
                       :rel "avatar"}]})
           (when display-name
             {:displayName display-name}))))

(defsection show-section [User :atom]
  [user & _]
  (user->person user))

(defsection show-section [User :html]
  [user & options]
  [:div.vcard.user-full
   (merge {:data-type "user"}
          (when-not *dynamic*
            {:data-id (:_id user)}))
   [:div (display-avatar user 96)]
   [:p
    [:span.nickname.fn.n
     [:span
      (if *dynamic*
        (bind-property "displayName")
        (:display-name user))]]
    " ("
    [:span
     (if *dynamic*
       (bind-property "username")
       (:username user))]
    "@"
    [:span
     (if *dynamic*
       (bind-property "domain")
       (link-to (actions.user/get-domain user)))] ")"]
   [:div.adr
    [:p.locality
     (if *dynamic*
       (bind-property "location")
       (:location user))]]
   [:p.note
    (if *dynamic*
      (bind-property "bio")
      (:bio user))]
   ;; [:p [:a {:href (:id user)} (:id user)]]
   ;; [:p [:a.url {:rel "me" :href (:url user)} (:url user)]]
   (when-not *dynamic*
     (when (:discovered user)
       (show-section (model.key/get-key-for-user user))))
   (user-actions user)])

(defsection show-section [User :model]
  [user & _]
  user)

(defsection show-section [User :rdf]
  [user & _]
  (let [{:keys [url display-name avatar-url first-name
                last-name username name email]} user
                mkp (try (model.key/get-key-for-user user)
                         (catch Exception ex))
                document-uri (str (full-uri user) ".rdf")
                user-uri (rdf/rdf-resource (str (full-uri user) "#me"))
                acct-uri (rdf/rdf-resource (model.user/get-uri user))]
    (rdf/with-rdf-ns ""
      (concat
       ;; TODO: describing the document should be the relm of the view
       (with-subject document-uri
         [[[ns/rdf  :type]                    [ns/foaf :PersonalProfileDocument]]
          [[ns/foaf :title]                   (rdf/l (str display-name "'s Profile"))]
          [[ns/foaf :maker]                   user-uri]
          [[ns/foaf :primaryTopic]            user-uri]])

       (with-subject user-uri
         (concat [[[ns/rdf  :type]                  [ns/foaf :Person]]
                  [[ns/foaf :weblog]                (rdf/rdf-resource (full-uri user))]
                  [[ns/foaf :holdsAccount]          acct-uri]]
                 (when mkp          [[(rdf/rdf-resource (str ns/cert "key")) (rdf/rdf-resource (str (full-uri user) "#key"))]])
                 (when username     [[[ns/foaf :nick]                        (rdf/l username)]])
                 (when name         [[[ns/foaf :name]                        (rdf/l name)]])
                 (when url          [[[ns/foaf :homepage]                    (rdf/rdf-resource url)]])
                 (when avatar-url   [[[ns/foaf :img]                         (rdf/rdf-resource avatar-url)]])
                 (when email        [[[ns/foaf :mbox]                        (rdf/rdf-resource (str "mailto:" email))]])
                 (when display-name [[[ns/foaf :name]                        (rdf/l display-name)]])
                 (when first-name   [[[ns/foaf :givenName]                   (rdf/l first-name)]])
                 (when last-name    [[[ns/foaf :familyName]                  (rdf/l last-name)]])))

       ;; (when mkp (show-section mkp)))

       (with-subject acct-uri
         [[[ns/rdf  :type]                    [ns/sioc "UserAccount"]]
          [[ns/foaf :accountServiceHomepage]  (rdf/rdf-resource (full-uri user))]
          [[ns/foaf :accountName]             (rdf/l (:username user))]
          [[ns/foaf :accountProfilePage]      (rdf/rdf-resource (full-uri user))]
          [[ns/sioc :account_of]              user-uri]])
       ))))

(defsection show-section [User :viewmodel]
  [item & [page]]
  (->> #_(dissoc (dissoc item :links) :_id)
       item
       (map (fn [[k v]] [(camelize (name k) :lower)
                        v]))
       (into {})))

(defsection show-section [User :xml]
  [user & options]
  [:user
   [:id (:_id user)]
   [:name (:display-name user)]
   [:screen_name (:username user)]
   [:location (:location user)]
   [:description (:bio user)]
   [:profile_image_url (h/h (:avatar-url user))]
   [:url (:url user)]
   [:protected "false"]])

;; TODO: This should be the vcard format
(defsection show-section [User :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (h/html
     ["vcard"
      {"xmlns" ns/vcard}
      ["fn" ["text" (:display-name user)]]
      [:nickname (:username user)]
      [:url (:url user)]
      [:n
       [:given (:first-name user)]
       [:family (:last-name user)]
       [:middle (:middle-name user)]
       ]
      (when avatar-url
         ["photo"
          ["uri" avatar-url]])])))

(defsection update-button [User :html]
  [user & _]
  [:form {:method "post" :action (str "/users/" (:_id user) "/update")}
   [:button.btn.update-button {:type "submit" :title "Update"}
    [:i.icon-refresh] [:span.button-text "Update"]]])

(defsection uri [User]
  [user & options]
  (when-not *dynamic*
    (if (model.user/local? user)
      (str "/" (:username user))
      (str "/remote-user/" (:username user) "@" (:domain user)))))
