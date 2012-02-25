(ns jiksnu.sections.user-sections
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               sections)
        (ciste.sections [default :only [title    title-type
                                        uri      uri-type
                                        full-uri full-uri-type
                                        show-section show-section-format show-section-serialization
                                        delete-button delete-button-format
                                        link-to
                                        index-section index-section-type index-section-format]])
        (clj-gravatar [core :only [gravatar-image]])
        (jiksnu session)
        (plaza.rdf.vocabularies foaf))
  (:require (clj-tigase [element :as element])
            (hiccup [core :as h]
                    [form-helpers :as f])
            (jiksnu [abdera :as abdera]
                    [namespace :as ns])
            (jiksnu.actions [subscription-actions :as actions.subscription]
                            [user-actions :as actions.user])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [signature :as model.signature]
                          [subscription :as model.subscription]
                          [user :as model.user])
            (plaza.rdf [core :as rdf])
            (ring.util [codec :as codec]))
  (:import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
           java.math.BigInteger
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.model.Entry))

(defn user-timeline-link
  [user format]
  (str "http://" (:domain user)
       "/api/statuses/user_timeline/" (:_id user) "." format))

(defn user->person)



;; TODO: Move this to user
(defn add-author
  "Adds the supplied user to the atom entry"
  [^Entry entry ^User user]
  ;; TODO: Do we need to re-fetch here?
  (if-let [user (model.user/fetch-by-id (:_id user))]
    (let [name (:name user)
          jid  (model.user/get-uri user false)
          actor (.addExtension entry ns/as "actor" "activity")]
      (doto actor
        (.addSimpleExtension ns/atom "name" "" name)
        (.addSimpleExtension ns/atom "email" "" jid)
        (.addSimpleExtension ns/atom "uri" "" jid))
      (doto entry
        (.addExtension actor)
        (.addExtension (show-section user))))))

(defn discover-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/discover")}
   [:button.btn.discover-button {:type "submit"}
    [:i.icon-search] [:span.button-text "Discover"]]])

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
     [:a.url {:href (str "/users/" (:_id user))
              :title (:name user)}
      (display-avatar-img user size)]))

(defn edit-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/edit")}
   [:button.btn.edit-button {:type "submit"}
    [:i.icon-pencil] [:span.button-text "Edit"]]])

(defn register-form
  [user]
  [:form {:method "post" :action "/main/register"}
   [:fieldset
    [:legend "Register"]

    [:div.clearfix
     [:label {:for "username"} "Username"]
     [:div.input
      [:input {:type "text" :name "username"}]]]

    [:div.clearfix
     [:label {:for "password"} "Password"]
     [:div.input
      [:input {:type "password" :name "password"}]]]

    [:div.clearfix
     [:label {:for "confirm-password"} "Confirm Password"]
     [:div.input
      [:input {:type "password" :name "confirm-password"}]]]

    [:div.clearfix
     [:label {:for "email"} "Email"]
     [:div.input
      [:input {:type "email" :name "email"}]]]

    [:div.clearfix
     [:label {:for "display-name"} "Display Name"]
     [:div.input
      [:input {:type "text" :name "display-name"}]]]

    [:div.clearfix
     [:label {:for "location"} "Location"]
     [:div.input
      [:input {:type "text" :name "location"}]]]

    [:div.clearfix
     [:label {:for "accepted"} "I have checked the box"]
     [:div.input
      [:input {:type "checkbox" :name "accepted"}]]]


    [:div.actions
     [:input.btn.primary {:type "submit" :value "Register"}]]
    ]]

  )



(defn edit-form
  [user]
  [:form {:method "post" :action "/settings/profile"}
   [:fieldset
    [:legend "Edit User"]

    [:div.clearfix
     [:label {:for "username"} "Username"]
     [:div.input
      [:input {:type "text" :name "username" :value (:username user)}]]]

    [:div.clearfix
     [:label {:for "domain"} "Domain"]
     [:div.input
      [:input {:type "text" :name "domain" :value (:domain user)}]]]
    
    [:div.clearfix
     [:label {:for "display-name"} "Display Name"]
     [:div.input
      [:input {:type "text" :name "display-name" :value (:display-name user)}]]]
    
    [:div.clearfix
     [:label {:for "first-name"} "First Name:"]
     [:div.input
      [:input {:type "text" :name "first-name" :value (:first-name user) }]]]
    
    [:div.clearfix
     [:label {:for "last-name"} "Last Name"]
     [:div.input
      [:input {:type "text" :name "last-name" :vaue (:last-name user)}]]]

    [:div.clearfix
     [:label {:for "email"} "Email"]
     [:div.input
      [:input {:type "email" :name "email" :value (:email user)}]]]

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

(defn update-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/update")}
   [:button.btn.update-button {:type "submit" :title "Update"}
    [:i.icon-refresh] [:span.button-text "Update"]]])

(defn subscribe-button
  [user]
  [:form {:method "post" :action (str "/users/" (:_id user) "/subscribe")}
   [:button.btn.subscribe-button {:type "submit" :title "Subscribe"}
    [:i.icon-eye-open] [:span.button-text "Subscribe"]]])

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
    (doto (.newAuthor abdera/*abdera-factory*)
      (.addSimpleExtension ns/as "object-type" "activity" ns/person)
      (.addSimpleExtension ns/atom "id" "" author-uri)
      (.setName (or (:name user)
                    (:display-name user)
                    (str (:first-name user) " " (:last-name user))))
      (.addSimpleExtension ns/atom "email" ""
                           (or (:email user) (model.user/get-uri user)))
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
            (.addSimpleExtension ns/poco "primary" "poco" "true"))))))

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
        [:li (subscribe-button user)])
      (when (is-admin?)
        [:li (delete-button user)])]]))

(defn add-form
  []
  [:form {:method "post" :action "/admin/users"}
   [:fieldset
    [:legend "Add User"]
    [:div.clearfix
     [:label {:type "username"} "Username"]
     [:div.input
      [:input {:type "text" :name "username"}]]]
    [:div.clearfix
     [:label {:for "domain"} "Domain"]
     [:div.input
      [:input {:type "text" :name "domain"}]]]
    [:div.actions
     [:input.btn.primary {:type "submit" :value "Add User"}]]]])

(defn push-subscribe-button
  [user]
  [:a.url {:href (:url user) :rel "contact"}
   [:span.fn.n (:display-name user)]])

(defn remote-warning
  [user]
  (when-not (:local user)
    [:p "This is a cached copy of information for a user on a different system"]))







;; TODO: make defsection
(defn index-line
  [user]
  (let [authenticated (current-user)]
    [:tr
     [:td (display-avatar user)]
     [:td
      [:p (link-to user)]
      [:p (:username user) "@" (:domain user)]
      [:p (:url user)]
      [:p (:bio user)]
      ]
     [:td
      [:ul.buttons
       [:li (subscribe-button user)]
       (when authenticated
         (list
          [:li (discover-button user)]
          [:li (update-button user)])
         (when (is-admin?)
           (list
            [:li (edit-button user)]
            [:li (delete-button user)])))
       ]]]))

(defsection index-section [User :html]
  [users & _]
  (list
   [:table.table.users
    [:thead]
    [:tbody
     (map index-line users)]]
   [:ul.pager
    [:li.previous [:a {:href "#"} "&larr; Previous"]]
    [:li.next [:a {:href "#"} "Next &rarr;"]]])
  )


(defsection title [User]
  [user & options]
  (or (:name user)
      (:display-name user)
      (:first-name user)
      (model.user/get-uri user)))


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
             {:image [{:url avatar-url}]})
           (when display-name
             {:displayName display-name}))))

(defsection show-section [User :atom]
  [user & _]
  (user->person user))

(defsection show-section [User :xml]
  [user & options]
  [:user
   [:id (:_id user)]
   [:name (:display-name user)]
   [:screen_name (:username user)]
   [:location (:location user)]
   [:description (:bio user)]
   [:profile_image_url (h/escape-html (:avatar-url user))]
   [:url (:url user)]
   [:protected "false"]])

(defsection show-section [User :html]
  [user & options]
  [:div.vcard.user-full
   [:p
    (display-avatar user)
    [:span.nickname.fn.n (:display-name user)]
    " (" (:username user) "@" (:domain user) ")"]
   [:div.adr
    [:p.locality (:location user)]]
   [:p.note (:bio user)]
   [:p [:a.url {:rel "me" :href (:url user)} (:url user)]]
   (user-actions user)])


;; (defsection show-section [User :n3]
;;   [user & options]
;;   (with-format :rdf
;;     (apply show-section user options)))

(defsection show-section [User :rdf]
  [user & _]
  (let [{:keys [url display-name avatar-url first-name last-name username name email]} user
        mkp (model.signature/get-key-for-user user)]
    (rdf/with-rdf-ns ""
      [
       ;; About the document
       [(str (full-uri user) ".rdf")
        [rdf/rdf:type                    foaf:PersonalProfileDocument
         [foaf :title]                 (rdf/l (str display-name "'s Profile"))
         [foaf :maker]               (rdf/rdf-resource (str (full-uri user) "#me"))
         foaf:primaryTopic           (rdf/rdf-resource (str (full-uri user) "#me"))]]
       

       ;; About the User
       [(rdf/rdf-resource (str (full-uri user) "#me"))
        (concat [rdf/rdf:type                    [foaf :Person]
                 foaf:weblog                     (rdf/rdf-resource (full-uri user))
                 [ns/foaf "holdsAccount"]        (rdf/rdf-resource (model.user/get-uri user))]
                (when mkp          [(rdf/rdf-resource (str ns/cert "key")) (rdf/rdf-resource (str (full-uri user) "#key"))])
                (when username     [foaf:nick       (rdf/l username)])
                (when name         [foaf:name       (rdf/l name)])
                (when url          [foaf:homepage   (rdf/rdf-resource url)])
                (when avatar-url   [foaf:img        avatar-url])
                (when email        [foaf:mbox       (rdf/rdf-resource (str "mailto:" email))])
                (when display-name [[foaf :name]    (rdf/l display-name)])
                (when first-name   [foaf:givenName  (rdf/l first-name)])
                (when last-name    [foaf:familyName (rdf/l last-name)]))]

       (when mkp
         [(rdf/rdf-resource (str (full-uri user) "#key"))
          [rdf/rdf:type (rdf/rdf-resource (str ns/cert "RSAPublicKey"))
           (rdf/rdf-resource (str ns/cert "identity")) (rdf/rdf-resource (str (full-uri user) "#me"))
           (rdf/rdf-resource (str ns/cert "exponent")) (rdf/l (:public-exponent mkp))
           (rdf/rdf-resource (str ns/cert "modulus")) (rdf/rdf-typed-literal
                                                       (.toString
                                                        (BigInteger.
                                                         (:modulus mkp)) 16)
                                                       (str ns/xsd "#hexBinary"))]])

       ;; About the User's Account
       [(rdf/rdf-resource (model.user/get-uri user))
        [rdf/rdf:type                [ns/sioc "UserAccount"]
         foaf:accountServiceHomepage (rdf/rdf-resource (full-uri user))
         foaf:accountName            (rdf/l (:username user))
         [foaf "accountProfilePage"] (rdf/rdf-resource (full-uri user))
         [ns/sioc "account_of"]         (rdf/rdf-resource
                                         (str (full-uri user) "#me"))]]])))

(defsection show-section [User :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (element/make-element
     "vcard" {"xmlns" ns/vcard}
     (if name
       ["fn" {}
        ["text" {} name]])
     (if avatar-url
       ["photo" {}
        ["uri" {} avatar-url]]))))

(defsection delete-button [User :html]
  [user & _]
  [:form {:method "post" :action (str "/users/" (:_id user) "/delete")}
   [:button.btn.delete-button {:type "submit" :title "Delete"}
    [:i.icon-trash] [:span.button-text "Delete"]]])


(defsection uri [User]
  [user & options]
  (if (model.user/local? user)
    (str "/" (:username user))
    (str "/users/" (:_id user))))

