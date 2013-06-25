(ns jiksnu.sections.user-sections
  (:use  [ciste.config :only [config]]
         [ciste.sections :only [defsection]]
         [ciste.sections.default :only [title uri full-uri show-section add-form
                                        edit-button delete-button link-to index-line
                                        show-section-minimal update-button index-block
                                        index-section]]
         [clojure.core.incubator :only [-?>]]
         [inflections.core :only [camelize]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.sections :only [action-link actions-section admin-actions-section
                                 admin-index-block admin-index-line admin-index-section
                                 admin-show-section bind-property bind-to control-line
                                 display-property dropdown-menu pagination-links]]
         [jiksnu.session :only [current-user is-admin?]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
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
            [jiksnu.rdf :as rdf]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [plaza.rdf.core :as plaza]
            [ring.util.codec :as codec])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User
           org.apache.abdera.model.Entry))

(defn user-timeline-link
  [user format]
  (str "http://" (config :domain)
       "/api/statuses/user_timeline/" (:_id user) "." format))

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
               {:data-bind "attr: {href: \"/users/\" + _id(), title: displayName}"}
               {:href (full-uri user)
                :title (:name user)})
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

(defn user->person
  [user]
  (let [author-uri (or (:url user)
                       (model.user/get-uri user))
        name (or (:name user)
                 (str (:first-name user) " " (:last-name user)))
        id (or (:id user) author-uri)
        extensions [{:ns ns/as
                     :local "object-type"
                     :prefix "activity"
                     :element ns/person}]
        params {:name name
                :extension extensions}
        person (abdera/make-person params)]
    (doto person

      (.addSimpleExtension ns/as   "object-type"       "activity" ns/person)
      (.addSimpleExtension ns/atom "id"                ""         id)
      (.addSimpleExtension ns/atom "uri"               ""         author-uri)
      (.addSimpleExtension ns/poco "preferredUsername" "poco"     (:username user))
      (.addSimpleExtension ns/poco "displayName"       "poco"     (title user))

      (.addExtension (doto (.newLink abdera/abdera-factory)
                       (.setHref (:avatarUrl user))
                       (.setRel "avatar")
                       (.setMimeType "image/jpeg")))
      (.addExtension (doto (.newLink abdera/abdera-factory)
                       (.setHref author-uri)
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

    person))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sections
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defsection admin-index-block [User :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

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

(defsection admin-index-section [User :viewmodel]
  [items & [page]]
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

(defsection index-block [User :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

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

(defsection index-line [User :model]
  [item & page]
  (show-section item page))

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

;; show-section

(defsection show-section [User :as]
  [user & options]
  (let [{:keys [display-name id avatar-url]} user
        avatar-url (or avatar-url (model.user/image-link user))]
    (merge {:profileUrl (full-uri user)
            :id (or id (model.user/get-uri user))
            :url (or (:url user)
                     (full-uri user))
            :type "person"
            :username (:username user)
            :domain (:domain user)
            :published (:updated user)
            ;; :name {:formatted (:name user)
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

(defsection show-section [User :json]
  [user & _]
  {:name (:name user)
   :id (:_id user)
   :screen_name (:username user)
   :url (:id user)
   :profile_image_url (:avatarUrl user)
   :protected false})

(defsection show-section [User :model]
  [user & _]
  (dissoc user :links))

(defsection show-section [User :rdf]
  [user & _]
  (let [{:keys [url display-name avatar-url first-name
                last-name username name email]} user
                mkp (try+ (model.key/get-key-for-user user)
                          (catch Exception ex
                            (trace/trace "errors:handled" ex)))
                document-uri (str (full-uri user) ".rdf")
                user-uri (plaza/rdf-resource (str (full-uri user) "#me"))
                acct-uri (plaza/rdf-resource (model.user/get-uri user))]
    (plaza/with-rdf-ns ""
      (concat
       ;; TODO: describing the document should be the relm of the view
       (rdf/with-subject document-uri
         [[[ns/rdf  :type]                    [ns/foaf :PersonalProfileDocument]]
          [[ns/foaf :title]                   (plaza/l (str display-name "'s Profile"))]
          [[ns/foaf :maker]                   user-uri]
          [[ns/foaf :primaryTopic]            user-uri]])

       (rdf/with-subject user-uri
         (concat
          [[[ns/rdf  :type]                  [ns/foaf :Person]]
           [[ns/foaf :weblog]                (plaza/rdf-resource (full-uri user))]
           [[ns/foaf :holdsAccount]          acct-uri]]
          (when mkp          [[(plaza/rdf-resource     (str ns/cert "key"))
                               (plaza/rdf-resource     (str (full-uri user) "#key"))]])
          (when username     [[[ns/foaf :nick]       (plaza/l username)]])
          (when name         [[[ns/foaf :name]       (plaza/l name)]])
          (when url          [[[ns/foaf :homepage]   (plaza/rdf-resource url)]])
          (when avatar-url   [[[ns/foaf :img]        (plaza/rdf-resource avatar-url)]])
          (when email        [[[ns/foaf :mbox]       (plaza/rdf-resource (str "mailto:" email))]])
          (when display-name [[[ns/foaf :name]       (plaza/l display-name)]])
          (when first-name   [[[ns/foaf :givenName]  (plaza/l first-name)]])
          (when last-name    [[[ns/foaf :familyName] (plaza/l last-name)]])))

       (rdf/with-subject acct-uri
         [[[ns/rdf  :type]                    [ns/sioc "UserAccount"]]
          [[ns/foaf :accountServiceHomepage]  (plaza/rdf-resource (full-uri user))]
          [[ns/foaf :accountName]             (plaza/l (:username user))]
          [[ns/foaf :accountProfilePage]      (plaza/rdf-resource (full-uri user))]
          [[ns/sioc :account_of]              user-uri]])))))

(defsection show-section [User :model]
  [item & [page]]
  (->> (dissoc item :links)
       ;; item

       (map (fn [[k v]] [(camelize (name k) :lower)
                         v]))
       (into {})))

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
   [:name (:name user)]
   [:screen_name (:username user)]
   [:location (:location user)]
   [:description (:bio user)]
   [:profile_image_url (h/h (:avatarUrl user))]
   [:url (:url user)]
   [:protected "false"]])

;; TODO: This should be the vcard format
(defsection show-section [User :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (h/html
     ["vcard"
      {"xmlns" ns/vcard}
      ["fn" ["text" (:name user)]]
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

;; title

(defsection title [User]
  [user & options]
  (or (:name user)
      (:first-name user)
      (model.user/get-uri user)))

;; update-button

(defsection update-button [User :html]
  [item & _]
  (action-link "user" "update" (:_id item)))

;; uri

(defsection uri [User]
  [user & options]
  (when-not *dynamic*
    (if (model.user/local? user)
      (str "/" (:username user))
      (str "/remote-user/" (:username user) "@" (:domain user)))))
