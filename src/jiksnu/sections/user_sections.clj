(ns jiksnu.sections.user-sections
  (:use (ciste [config :only (config)]
               [debug :only (spy)]
               html
               sections)
        ciste.sections.default
        (clj-gravatar [core :only (gravatar-image)])
        (jiksnu model session view)
        (plaza.rdf core)
        (plaza.rdf.vocabularies foaf))
  (:require (clj-tigase [element :as element])
            (hiccup [form-helpers :as f])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace])
            (jiksnu.actions [subscription-actions :as actions.subscription])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.templates [user :as templates.user]))
  (:import java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.model.Entry))


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
     (.setObjectType namespace/person)
     (.setId author-uri)
     (.setName (:first-name user) (:last-name user))
     (.setDisplayName (:name user))
     (.addSimpleExtension namespace/atom "email" ""
                          (or (:email user) (model.user/get-uri user)))
     (.addSimpleExtension namespace/atom "name" "" (:name user))
     (.addAvatar (:avatar-url user) "image/jpeg")
     (.addLink (:avatar-url user) "avatar")
     (.addSimpleExtension namespace/atom "uri" "" author-uri)
     (.addSimpleExtension namespace/poco "preferredUsername" "poco" (:username user))
     (.addSimpleExtension namespace/poco "displayName" "poco" (title user))
     (-> (.addLink (full-uri user) "alternate")
         (.setMimeType "text/html"))
     (-> (.addExtension namespace/status "profile_info" "statusnet")
         (.setAttributeValue "local_id" (str (:_id user))))
     (-> (.addExtension namespace/poco "urls" "poco")
         (doto (.addSimpleExtension namespace/poco "type" "poco" "homepage")
           (.addSimpleExtension namespace/poco "value" "poco" (full-uri user))
           (.addSimpleExtension namespace/poco "primary" "poco" "true"))))))


(defsection show-section [User :atom]
  [user & _]
  (user->person user))

(defsection show-section [User :html]
  [user & options]
  (templates.user/show user))


(defsection show-section [User :rdf]
  [user & _]
  (with-rdf-ns ""
    [[(str (full-uri user) ".rdf")
      [rdf:type                    foaf:PersonalProfileDocument
       [foaf :maker]               (full-uri user)
       foaf:primaryTopic           (rdf-resource
                                    (model.user/get-uri user))]]
     [(rdf-resource (model.user/get-uri user))
      [rdf:type                    [foaf :Person]
       [foaf :name]                (l (:name user))
       foaf:nick                   (l (:username user))
       foaf:name                   (l (:name user))
       foaf:mbox                   (rdf-resource
                                    (str "mailto:" (:email user)))
       foaf:givenName              (l (:first-name user))
       foaf:familyName             (l (:last-name user))
       foaf:homepage               (rdf-resource (:url user))
       foaf:weblog                 (rdf-resource (full-uri user))
       foaf:img                    (:avatar-url user)
       foaf:account                (rdf-resource
                                    (str (full-uri user) "#acct"))]]
     [(rdf-resource (str (full-uri user) "#acct"))
      [rdf:type                    foaf:OnlineAccount
       foaf:accountServiceHomepage (rdf-resource (full-uri user))
       foaf:accountName            (l (:username user))
       [foaf "accountProfilePage"] (rdf-resource (full-uri user))
       [namespace/sioc "account_of"]         (rdf-resource
                                    (model.user/get-uri user))]]]))

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


(defsection show-section [User :xmpp :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (element/make-element
     "vcard" {"xmlns" namespace/vcard}
     (if name
       ["fn" {}
        ["text" {} name]])
     (if avatar-url
       ["photo" {}
        ["uri" {} avatar-url]]))))

;; (defsection show-section-minimal [User :xmpp :xmpp]
;;   [property & options]
;;   (element/make-element
;;    (:key property) {}
;;    [(:type property) {} (:value property)]))
