(ns jiksnu.templates.user
  (:use ciste.debug
        closure.templates.core
        [clj-gravatar.core :only (gravatar-image)]))

(defn format-data
  [user]
  {:id (str (:_id user))
   :name (str (:username user) "@" (:domain user))
   :username (:username user)
   :domain (:domain user)
   :url (str "/users/" (:_id user))
   :local (:local user)
   :hub (:hub user)
   :admin (:admin user)
   :links []
   :subscriptions []
   :subscribers []
   :display-name
   (or (:display-name user)
       (str (:first-name user) " " (:last-name user)))
   :imgsrc (or (:avatar-url user)
               (and (:email user)
                    (gravatar-image (:email user)))
               (gravatar-image (:jid user))
               (gravatar-image (str (:username user) "@" (:domain user)))
               "")})

(deftemplate show
  [user]
  (format-data user))

(deftemplate register-section
  [request]
  {:username ""
   :password ""
   :confirm-password ""})

(deftemplate show-minimal
  [user]
  {:user user})

(deftemplate edit-form
  [{id :_id
    :keys [username display-name first-name last-name domain
           email bio password confirm-password url location]
    :as user}]
  {:id (str id)
   :username username
   :domain domain
   :location location
   :display-name display-name
   :first-name first-name
   :last-name last-name
   :email email
   :local true
   :links {}
   :bio bio
   :password password
   :confirm-password confirm-password
   :url url})

(deftemplate index-section
  [users]
  {:users (map format-data users)})
