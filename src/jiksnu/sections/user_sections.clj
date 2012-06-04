(ns jiksnu.sections.user-sections
  (:use  [ciste.config :only [config]]
         ciste.sections
         [ciste.sections.default :only [title uri full-uri show-section add-form edit-button
                                        delete-button link-to index-line
                                        update-button index-section]]
         [clj-gravatar.core :only [gravatar-image]]
         [jiksnu.model :only [with-subject]]
         jiksnu.session
         [jiksnu.views :only [control-line]]
         plaza.rdf.vocabularies.foaf)
  (:require [clj-tigase.element :as element]
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
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [plaza.rdf.core :as rdf]
            [ring.util.codec :as codec])
  (:import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
           java.math.BigInteger
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
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
   {:width size
    :height size
    :alt ""
    :src (model.user/image-link user)}])

(defn display-avatar
  ([user] (display-avatar user 48))
  ([user size]
     [:a.url {:href (full-uri user)
              :title (:name user)}
      (display-avatar-img user size)]))

(defn register-form
  [user]
  [:form.well.form-horizontal {:method "post" :action "/main/register"}
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

    [:div.clearfix
     [:label {:for "bio"} "Bio"]
     [:div.input
      [:textarea {:name "bio"}
       (:bio user)]]]

    [:div.clearfix
     [:label {:for "location"} "Location"]
     [:div.input
      [:input {:type "text" :name "location" :value (:location user)}]]]

    [:div.clearfix
     [:label {:for "url"} "Url"]
     [:div.input
      [:input {:type "text" :name "url" :value (:url user)}]]]

    [:div.actions
     [:input.btn.primary {:type "submit" :value "submit"}]]]]
  
  )

(defn following-section
  [user]
  (let [authenticated (current-user)]
    (list
     (when (model.subscription/subscribing? user authenticated)
       [:p "This user follows you"])
     (when (model.subscription/subscribed? user authenticated)
       [:p "You follow this user"]))))

(defn admin-index-line
  [user]
  (let [domain (actions.user/get-domain user)]
    [:tr
     [:td (display-avatar user)]
     [:td (link-to user)]
     [:td (link-to domain)]
     [:td (discover-button user)]
     [:td (update-button user)]
     [:td (edit-button user)]
     [:td (delete-button user)]]))

(defn user->person
  [user]
  (let [author-uri (model.user/get-uri user)]
    (let [person (.newAuthor abdera/*abdera-factory*)]
      (doto person
       (.addSimpleExtension ns/as "object-type" "activity" ns/person)
       (.addSimpleExtension ns/atom "id" "" (or (:id user)
                                                author-uri))
       (.setName (or (:name user)
                     (:display-name user)
                     (str (:first-name user) " " (:last-name user))))
      
       (.addExtension (doto (.newLink abdera/*abdera-factory*)
                        (.setHref (:avatar-url user))
                        (.setRel "avatar")
                        (.setMimeType "image/jpeg")))
       
       (.addSimpleExtension ns/atom "uri" "" author-uri)

       (.addSimpleExtension ns/poco "preferredUsername" "poco" (:username user))
       (.addSimpleExtension ns/poco "displayName" "poco" (title user))
       (.addExtension (doto (.newLink abdera/*abdera-factory*)
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



(defsection title [User]
  [user & options]
  (or (:name user)
      (:display-name user)
      (:first-name user)
      (model.user/get-uri user)))

(defsection uri [User]
  [user & options]
  (if (model.user/local? user)
    (str "/" (:username user))
    (str "/remote-user/" (:username user) "@" (:domain user))))





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

(defsection index-line [User :html]
  [user & _]
  (let [authenticated (current-user)]
    [:tr
     [:td (display-avatar user)]
     [:td
      [:p (link-to user)]
      [:p (:username user) "@" (:domain user)]
      [:p (:url user)]
      [:p (:bio user)]]
     [:td
      [:ul.buttons
       [:li (subscribe-button user)]
       (when authenticated
         (list
          [:li (discover-button user)]
          [:li (update-button user)]
          (when (is-admin?)
            (list
             [:li (edit-button user)]
             [:li (delete-button user)]))))]]]))

(defsection index-section [User :html]
  [users & [options & _]]
  (let [{:keys [page total-records]} options]
    (list
     [:p "Page " page]
     [:p "Total Records: " total-records]
     [:table.table.users
      [:thead]
      [:tbody
       (map index-line users)]]
     (pagination-links options))))


(defsection show-section [User :html]
  [user & options]
  [:div.vcard.user-full
   [:p
    (display-avatar user)
    [:span.nickname.fn.n (:display-name user)]
    " (" (:username user) "@" (link-to (actions.user/get-domain user)) ")"]
   [:div.adr
    [:p.locality (:location user)]]
   [:p.note (:bio user)]
   [:p [:a {:href (:id user)} (:id user)]]
   [:p [:a.url {:rel "me" :href (:url user)} (:url user)]]
   (when (:discovered user)
     (show-section (model.key/get-key-for-user user)))
   (user-actions user)])

(defsection update-button [User :html]
  [user & _]
  [:form {:method "post" :action (str "/users/" (:_id user) "/update")}
   [:button.btn.update-button {:type "submit" :title "Update"}
    [:i.icon-refresh] [:span.button-text "Update"]]])





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
       (with-subject document-uri
         [[rdf/rdf:type                    foaf:PersonalProfileDocument]
          [[foaf :title]                   (rdf/l (str display-name "'s Profile"))]
          [[foaf :maker]                   user-uri]
          [foaf:primaryTopic               user-uri]])
       (with-subject user-uri
         (concat [[rdf/rdf:type                    [foaf :Person]]
                  [foaf:weblog                     (rdf/rdf-resource (full-uri user))]
                  [[ns/foaf "holdsAccount"]        acct-uri]]
                 (when mkp          [[(rdf/rdf-resource (str ns/cert "key")) (rdf/rdf-resource (str (full-uri user) "#key"))]])
                 (when username     [[foaf:nick       (rdf/l username)]])
                 (when name         [[foaf:name       (rdf/l name)]])
                 (when url          [[foaf:homepage   (rdf/rdf-resource url)]])
                 (when avatar-url   [[foaf:img        avatar-url]])
                 (when email        [[foaf:mbox       (rdf/rdf-resource (str "mailto:" email))]])
                 (when display-name [[[foaf :name]    (rdf/l display-name)]])
                 (when first-name   [[foaf:givenName  (rdf/l first-name)]])
                 (when last-name    [[foaf:familyName (rdf/l last-name)]]))


         )
       (when mkp (show-section mkp))
       (with-subject acct-uri
         [[rdf/rdf:type                [ns/sioc "UserAccount"]]
          [foaf:accountServiceHomepage (rdf/rdf-resource (full-uri user))]
          [foaf:accountName            (rdf/l (:username user))]
          [[foaf "accountProfilePage"] (rdf/rdf-resource (full-uri user))]
          [[ns/sioc "account_of"]      user-uri]])))))





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
