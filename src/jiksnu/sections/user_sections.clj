(ns jiksnu.sections.user-sections
  (:use (ciste [config :only (config)]
               [debug :only (spy)]
               sections)
        ciste.sections.default
        (clj-gravatar [core :only (gravatar-image)])
        (jiksnu model session view)
        (plaza.rdf.vocabularies foaf))
  (:require (clj-tigase [element :as element])
            (hiccup [form-helpers :as f])
            (jiksnu [abdera :as abdera]
                    [namespace :as ns])
            (jiksnu.actions [subscription-actions :as actions.subscription])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [signature :as model.signature]
                          [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.templates [user :as templates.user])
            (plaza.rdf [core :as rdf]))
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

(defsection show-section [User :html]
  [user & options]
  (templates.user/show user))


;; (defsection show-section [User :n3]
;;   [user & options]
;;   (with-format :rdf
;;     (apply show-section user options)))

(defsection show-section [User :rdf]
  [user & _]
  (let [{:keys [url display-name avatar-url first-name last-name username name email]} user]
    (rdf/with-rdf-ns ""
      [
       ;; About the document
       [(str (full-uri (spy user)) ".rdf")
          [rdf/rdf:type                    foaf:PersonalProfileDocument
           [foaf :maker]               (rdf/rdf-resource (str (full-uri user) "#me"))
           foaf:primaryTopic           (rdf/rdf-resource (str (full-uri user) "#me"))]]
       

       ;; About the User
       [(rdf/rdf-resource (str (full-uri user) "#me"))
        (concat [rdf/rdf:type                    [foaf :Person]
                 foaf:weblog                     (rdf/rdf-resource (full-uri user))
                 [ns/foaf "holdsAccount"]        (rdf/rdf-resource (model.user/get-uri user))
                 

                 ]
                #_(let [mkp (model.signature/get-key-for-user user)]
                  [(rdf/rdf-resource (str ns/cert "key"))
                   [
                    rdf/rdf:type        [foaf :Person]
                    #_(rdf/l (str ns/cert "RSAPublicKey"))
                    ;; (rdf/rdf-resource (str ns/cert "modulus")) (rdf/l (or (:armored-n (spy mkp)) " "))
                    ;; (rdf/rdf-resource (str ns/cert "exponent")) (rdf/l (:public-exponent mkp))
                    ]
                   
                   ]
                   )
                (when username     [foaf:nick       (rdf/l username)])
                (when name         [foaf:name       (rdf/l name)])
                (when url          [foaf:homepage   (rdf/rdf-resource url)])
                (when avatar-url   [foaf:img        avatar-url])
                (when email        [foaf:mbox       (rdf/rdf-resource (str "mailto:" email))])
                (when display-name [[foaf :name]    (rdf/l display-name)])
                (when first-name   [foaf:givenName  (rdf/l first-name)])
                (when last-name    [foaf:familyName (rdf/l last-name)])
                
                )

        ]

       ;; About the User's Account
       [(rdf/rdf-resource (model.user/get-uri user))
          [rdf/rdf:type                [ns/sioc "UserAccount"]
           foaf:accountServiceHomepage (rdf/rdf-resource (full-uri user))
           foaf:accountName            (rdf/l (:username user))
           [foaf "accountProfilePage"] (rdf/rdf-resource (full-uri user))
           [ns/sioc "account_of"]         (rdf/rdf-resource
                                                  (model.user/get-uri user))]]


       ])))

(defsection show-section [User :json]
  [user & options]
  (let [{:keys [display-name id avatar-url]} user]
    (merge {:profileUrl (full-uri user)
            :id (or id (model.user/get-uri user))
            :url (full-uri user)
            :objectType "person"
            ;; :name {:formatted (:display-name user)
            ;;        :familyName (:last-name user)
            ;;        :givenName (:first-name user)}
            }
           (when avatar-url
             ;; TODO: get image dimensions
             {:image [{:url avatar-url}]})
           (when display-name
             {:displayName display-name})
          )))


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

;; (defsection show-section-minimal [User :xmpp :xmpp]
;;   [property & options]
;;   (element/make-element
;;    (:key property) {}
;;    [(:type property) {} (:value property)]))
