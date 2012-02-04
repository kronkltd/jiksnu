(ns jiksnu.sections.user-sections
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               sections)
        (ciste.sections [default :only [title    title-type
                                        uri      uri-type
                                        full-uri full-uri-type
                                        show-section-format show-section-serialization
                                        delete-button delete-button-format
                                        link-to
                                        ]])
        (clj-gravatar [core :only [gravatar-image]])
        (jiksnu session)
        (plaza.rdf.vocabularies foaf))
  (:require (clj-tigase [element :as element])
            (hiccup [form-helpers :as f])
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
            (plaza.rdf [core :as rdf]))
  (:import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
           java.math.BigInteger
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.model.Entry))

(defn discover-button
  [user]
  [:form {:method "post" :action (str "/users/" (:id user) "/discover")}
   [:input.btn.discover-button {:type "submit" :value "Discover"}]])

(defn display-avatar
  ([user] (display-avatar user 48))
  ([user size]
     [:a {:href (str "/users/" (:_id user))
          :title (:name user)}
      [:img.avatar.photo
       {:width size
        :height size
        :alt ""
        :src (model.user/image-link user)}]]))

(defn edit-button
  [user]
  [:form {:method "post" :action (str "/users/" (:id user) "/edit")}
   [:input.btn.edit-button {:type "submit" :value "Edit"}]])

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
  [:form {:method "post" :action (str "/users/" (:id user) "/update")}
   [:input.btn.update-button {:type "submit" :value "Update"}]])

(defn subscribe-button
  [user]
  [:form {:method "post" :action (str "/users/" (:id user) "/subscribe")}
   [:input.btn.subscribe-button {:type "submit" :value "Subscribe"}]])

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
    (str "/users/" (:_id user))))

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


(defsection show-section [User :atom]
  [user & _]
  (user->person user))

(defn user-actions
  [user]
  (if-let [authenticated (current-user)]
    [:div
     (when (= (:_id user) (:_id authenticated))
       [:p "This is you"])
     [:ul.user-actions
      [:li (discover-button user)]
      [:li (update-button user)]
      (when (not= (:_id user) (:_id authenticated))
        [:li (subscribe-button user)])]]))

(defsection show-section [User :html]
  [user & options]
  [:div.vcard
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
        mk (model.signature/get-key-for-user user)]
    (rdf/with-rdf-ns ""
      [
       ;; About the document
       [(str (full-uri (spy user)) ".rdf")
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

(defsection show-section [User :json]
  [user & options]
  (let [{:keys [display-name id avatar-url]} user
        avatar-url (or avatar-url (model.user/image-link user))]
    (merge {:profileUrl (full-uri user)
            :id (or id (model.user/get-uri user))
            :url (full-uri user)
            :objectType "person"
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


(defsection show-section [User :xmpp :xmpp]
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
  [:form {:method "post" :action (str "/users/" (:id user) "/delete")}
   [:input.btn.delete-button {:type "submit" :value "Delete"}]])

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


  ;; (defsection show-section-minimal [User :xmpp :xmpp]
  ;;   [property & options]
  ;;   (element/make-element
  ;;    (:key property) {}
  ;;    [(:type property) {} (:value property)]))


(defn index-line
  [user]
  [:li
   [:p (display-avatar user)]
   [:p (link-to user)]
   [:ul
    [:li (subscribe-button user)]
    [:li (discover-button user)]
    [:li (update-button user)]
    [:li (edit-button user)]]])

(defn index-section
  [users]
  [:ul.users
   (map index-line users)])
