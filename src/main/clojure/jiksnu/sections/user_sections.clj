(ns jiksnu.sections.user-sections
  (:use (ciste config debug html sections)
        ciste.sections.default
        [clj-gravatar.core :only (gravatar-image)]
        jiksnu.abdera
        jiksnu.helpers.user-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.subscription-sections
        jiksnu.session
        jiksnu.view
        plaza.rdf.core
        plaza.rdf.vocabularies.foaf)
  (:require [clj-tigase.element :as element]
            [hiccup.form-helpers :as f]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.templates
             [user :as template.user]))
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show-section
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection show-section [User :atom]
  [^User user & options]
  (let [person (Person. (make-object atom-ns "author" ""))
        author-uri (full-uri user)]
    (doto person
      (.setObjectType person-uri)
      (.setId (str "acct:" (model.user/get-uri user)))
      (.setName (:first-name user) (:last-name user))
      (.setDisplayName (:name user))
      (.addSimpleExtension atom-ns "email" ""
                           (or (:email user) (model.user/get-uri user)))
      (.addSimpleExtension atom-ns "name" "" (:name user))
      (.addAvatar (:avatar-url user) "image/jpeg")
      (.addLink (:avatar-url user) "avatar")
      (.addSimpleExtension atom-ns "uri" "" author-uri)
      (.addSimpleExtension poco-ns "preferredUsername" "poco" (:username user))
      (.addSimpleExtension poco-ns "displayName" "poco" (title user)))
    (-> person
        (.addLink author-uri "alternate")
        (.setMimeType "text/html"))
    (-> (.addExtension person status-uri "profile_info" "statusnet")
        (.setAttributeValue "local_id" (str (:_id user))))
    (let [urls-element (.addExtension person poco-ns "urls" "poco")]
      (doto urls-element
        (.addSimpleExtension poco-ns "type" "poco" "homepage")
        (.addSimpleExtension poco-ns "value" "poco" (full-uri user))
        (.addSimpleExtension poco-ns "primary" "poco" "true")))
    person))

(defsection show-section [User :xmpp :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (element/make-element
     "vcard" {"xmlns" vcard-uri}
     (if name
       ["fn" {}
        ["text" {} name]])
     (if avatar-url
       ["photo" {}
        ["uri" {} avatar-url]]))))

(defsection show-section [User :html]
  [user & options]
  (template.user/show user)
  #_(let [actor (current-user-id)
        {id :_id
         username :username
         domain :domain
         url :url
         name :name
         hub :hub
         location :location
         bio :bio} user]
    (list
     [:div.vcard
      [:p (avatar-img user)]
      [:p
       [:span.nickname
        username
        (when-not (model.user/local? user)
          (list "@" domain))]]
      [:p.fn.n name]
      [:div.adr
       [:p.locality location]]
      [:p.note bio]
      [:p [:a.url {:href url :rel "me"} url]]
      [:p "Id: " id]
      [:p "Local: " (:local user)]
      [:p "Hub: " hub]
      [:p "Discovered: " (:discovered user)]
      [:p "Last Updated:" (:updated user)]
      #_(links-list user)]
     [:div.subscription-sections
      (following-section actor user)
      (user-actions user)
      (remote-subscribe-form user)
      (let [[_ records] (actions.subscription/subscriptions user)]
        (subscriptions-section records))
      (let [[_ records] (actions.subscription/subscribers user)]
        (subscribers-section records))])))

(defsection show-section [User :rdf]
  [user & _]
  (with-rdf-ns ""
    [[(str (full-uri user) ".rdf")
     [rdf:type foaf:PersonalProfileDocument
      [foaf :maker] (full-uri user)
      foaf:primaryTopic (rdf-resource (str "acct:" (model.user/get-uri user)))]]

    [(rdf-resource (str "acct:" (model.user/get-uri user)))
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
      [sioc "account_of"] (rdf-resource (str "acct:" (model.user/get-uri user)))]]]))

(defsection show-section [User :json]
  [user & options]
  {:profileUrl (full-uri user)
   :id (:_id user)
   :name {:formatted (:name user)
          :familyName (:last-name user)
          :givenName (:first-name user)}
   :photos [{:value (:avatar-url user)
             :type "thumbnail"}]
   :displayName (:name user)})

;; (defsection show-section-minimal [User :xmpp :xmpp]
;;   [property & options]
;;   (element/make-element
;;    (:key property) {}
;;    [(:type property) {} (:value property)]))

(defsection show-section-minimal [User :html]
  [user & options]
  [:div.vcard
   ;; (avatar-img user)
   (link-to user)])

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
