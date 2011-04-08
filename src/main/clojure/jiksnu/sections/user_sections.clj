(ns jiksnu.sections.user-sections
  (:use ciste.config
        ciste.html
        ciste.sections
        ciste.view
        [clj-gravatar.core :only (gravatar-image)]
        clj-tigase.core
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        plaza.rdf.core
        plaza.rdf.vocabularies.foaf)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry))

(defsection add-form [User]
  [record & options]
  [:div
   [:h3 "Create User"]
   (f/form-to
    [:post "/main/register"]
    (f/text-field :username)
    (f/submit-button "Add User"))])

(defsection edit-form [User]
  [user & options]
  (let [{:keys [domain first-name url email bio location last-name password
                confirm-password avatar-url]} user]
    [:div
     (f/form-to
      [:post (uri user)]
      [:fieldset
       [:legend "Edit User"]
       [:ul
        [:li (:username user)]
        [:li (f/label :domain "Domain: ")
         (f/text-field :domain domain)]
        [:li (f/label :name "Display Name: ")
         (f/text-field :name (:name user))]
        [:li (f/label :first-name "First Name: ")
         (f/text-field :first-name first-name)]
        [:li (f/label :last-name "Last Name: ")
         (f/text-field :last-name last-name)]
        [:li (f/label :email "Email: ")
         (f/text-field :email email)]
        [:li (f/label :bio "Bio: ")
         (f/text-field :bio bio)]
        [:li (f/label :location "Location: ")
         (f/text-field :location location)]
        [:li (f/label :url "Url: ")
         (f/text-field :url url)]
        [:li (f/label :password "Password: ")
         (f/text-field :password password)]
        [:li (f/label :confirm-password "Confirm Password: ")
         (f/text-field :confirm-password confirm-password)]
        [:li (f/label :admin "Admin?: ")
         (f/check-box :admin (:admin user))]
        [:li (f/label :debug "Debug?: ")
         (f/check-box :debug (:debug user))]
        [:li (f/label :avatar-url "Avatar Url: ")
         (f/text-field :avatar-url avatar-url)]]
       (f/submit-button "Submit")])
     (dump user)]))

(defsection link-to [User :html]
  [user & options]
  [:a.url {:href (uri user)
           :rel "contact"}
   [:span.fn.n (title user)]])

(defn make-object
  [namespace name prefix]
  (com.cliqset.abdera.ext.activity.Object.
   *abdera-factory* (QName. namespace name prefix)))

(defn get-uri
  [^User user]
  (str (:_id user) "@" (:domain user)))

(defn ^URI author-uri
  [^Entry entry]
  (let [author (.getAuthor entry)]
    (let [uri (.getUri author)]
      (URI. (.toString uri)))))

(defn vcard-request
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :get}))

(defn request-vcard!
  [user]
  (let [packet-map
        {:from (make-jid "" (:domain (config)))
         :to (make-jid user)
         :id "JIKSNU1"
         :type :get
         :body
         (make-element
          "query"
          {"xmlns" "http://onesocialweb.org/spec/1.0/vcard4#query"})}
        packet (make-packet packet-map)]
    (deliver-packet! packet)))

(defn avatar-img
  [user]
  (let [{:keys [avatar-url title email domain name]} user]
    (let [jid (str (:username user) "@" domain)]
      [:a.url {:href (uri user)
               :title title}
       [:img.avatar.photo
        {:width "48"
         :height "48"
         :src (or avatar-url
                  (and email (gravatar-image email))
                  (gravatar-image jid))}]])))

(defn subscribe-form
  [user]
  (f/form-to [:post "/main/subscribe"]
             (f/hidden-field :subscribeto (:_id user))
             (f/submit-button "Subscribe")))

(defn unsubscribe-form
  [user]
  (f/form-to [:post "/main/unsubscribe"]
             (f/hidden-field :unsubscribeto (:_id user))
             (f/submit-button "Unsubscribe")))

(defn user-actions
  [user]
  (let [actor-id (current-user-id)]
    (if (= (:_id user) actor-id)
      [:p "This is you!"]
      [:ul
       [:li
        (if (model.subscription/subscribing? actor-id (:_id user))
          (unsubscribe-form user)
          (subscribe-form user))]])))


(defn remote-subscribe-form
  [user]
  (list
   [:a.entity_remote_subscribe
    {:href (str "/main/ostatus?nickname="
                (:username user))}
    "Subscribe"]
   (f/form-to
    [:post "/main/ostatus"]
    [:fieldset
     [:legend "Subscribe to " (:username user)]
     [:ul.form_data
      [:li.ostatus_nickname
       (f/label :nickname "User nickname")
       (f/hidden-field :nickname (:username user))]
      [:li.ostatus_profile
       (f/label :profile "Profile Account")
       (f/text-field :profile)]]
     (f/submit-button "Submit")])))

(defn subscriptions-list
  [user]
  [:div#subscriptions
   [:h3 [:a {:href (str (uri user) "/subscriptions") }
         "Subscriptions"]]
   [:ul
    (map
     (fn [subscription]
       [:li (show-section-minimal
             (jiksnu.model.user/fetch-by-id (:to subscription)))
        (if (:pending subscription) " (pending)")])
     (model.subscription/subscriptions user))]
   [:p [:a {:href "/main/ostatussub"} "Add Remote"]]])

(defn subscribers-list
  [user]
  [:div#subscribers
   [:h3 [:a {:href (str (uri user) "/subscribers")}
         "Subscribers"]]
   [:ul
    (map
     (fn [subscriber]
       [:li (show-section-minimal
             (jiksnu.model.user/fetch-by-id (:from subscriber)))])
     (model.subscription/subscribers user))]])

(defn format-list
  [user]
  [:div#formats
   [:ul
    [:li
     [:a {:href (str (uri user) ".rdf")} "FOAF"]]
    [:li
     [:a {:href (str (uri user) ".n3")} "N3"]]
    [:li
     [:a {:href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user)
                     ".atom")} "Atom"]]
    [:li
     [:a {:href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user)
                     ".json")} "JSON"]]]])

(defn activities-list
  [user]
  [:div.activities
   (map show-section-minimal
        (model.activity/find-by-user user))])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection show-section [User :atom]
  [^User user & options]
  (let [person (Person. (make-object atom-ns "author" ""))
        author-uri (full-uri user)]
    (.setObjectType person person-uri)
    (.setId person (str "acct:" (get-uri user)))
    (.setName person (:first-name user) (:last-name user))
    (.setDisplayName person (:name user))
    (.addSimpleExtension person atom-ns "email" ""
                         (or (:email user) (get-uri user)))
    (.addSimpleExtension person atom-ns "name" "" (:name user))
    (.addAvatar person (:avatar-url user) "image/jpeg")
    (.addSimpleExtension person atom-ns "uri" "" author-uri)
    person))

(defsection show-section [User :xmpp :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (make-element
     "vcard" {"xmlns" vcard-uri}
     (if name
       ["fn" {}
        ["text" {} name]])
     (if avatar-url
       ["photo" {}
        ["uri" {} avatar-url]]))))

(defsection show-section [User :html]
  [user & options]
  (let [actor (current-user-id)]
    (list
     (add-form (Activity.))
     [:div
      [:div.aside
       (if (not= (:domain user) (:domain (config)))
         [:p.important
          "This is a cached copy of information for a user on a different system."])
       (let [{{id :_id
               username :username} user} user]
         [:div.vcard
          [:p (avatar-img user)]
          [:p
           [:span.nickname
            (:username user)
            (if (not= (:domain user) (:domain (config)))
              (list "@" (:domain user)))]
           [:span.fn.n (:name user)]]
          [:div.adr
           [:p.locality (:location user)]]
          [:p.note (:bio user)]
          [:p [:a.url {:href (:url user) :rel "me"} (:url user)]]
          [:p "Id: " (:_id user)]])
       (if actor
         (list
          (if (model.subscription/subscribed? actor (:_id user))
            [:p "This user follows you."])
          (if (model.subscription/subscribing? actor (:_id user))
            [:p "You follow this user."])))
       [:div.subscription-sections
        (user-actions user)
        (remote-subscribe-form user)
        (subscriptions-list user)
        (subscribers-list user)]
       (format-list user)
       (dump user)]
      (activities-list user)])))

(defsection show-section [User :rdf]
  [user & _]
  (with-rdf-ns ""
    [[(str (full-uri user) ".rdf")
     [rdf:type foaf:PersonalProfileDocument
      [foaf :maker] (full-uri user)
      foaf:primaryTopic (rdf-resource
                         (str "acct:" (:username user) "@"
                              (:domain user)))]]

    [(rdf-resource (str "acct:" (:username user) "@" (:domain user)))
     [rdf:type [foaf :Person]
      [foaf :name] (l (:name user))
      foaf:nick (l (:username user))
      foaf:name (l (:name user))
      foaf:mbox (rdf-resource (str "mailto:" (:email user)))
      foaf:givenName (l (:first-name user))
      foaf:familyName (l (:last-name user))
      foaf:homepage (rdf-resource (:url user))
      foaf:weblog (rdf-resource (full-uri user))
      foaf:img (:avatar-url user)
      foaf:account (rdf-resource (str (full-uri user) "#acct"))]]
    [(rdf-resource (str (full-uri user) "#acct"))
     [rdf:type foaf:OnlineAccount
      foaf:accountServiceHomepage (rdf-resource (str "http://" (:domain user)))
      foaf:accountName (l (:username user))
      [foaf "accountProfilePage"] (rdf-resource (full-uri user))
      [sioc "account_of"] (rdf-resource (str "acct:" (:username user) "@"
                                             (:domain user)))]]]))

;; (defsection show-section-minimal [User :xmpp :xmpp]
;;   [property & options]
;;   (make-element
;;    (:key property) {}
;;    [(:type property) {} (:value property)]))

(defsection show-section-minimal [User :html]
  [user & options]
  [:div.vcard
   (avatar-img user)
   (link-to user)])

(defsection title [User]
  [user & options]
  (or (:display-name user)
      (:first-name user)
      (str (:username user) "@" (:domain user))))

(defsection uri [User]
  [user & options]
  (if (= (:domain user) (:domain (config)))
    (str "/" (:username user))
    (str "/users/" (:_id user))))
