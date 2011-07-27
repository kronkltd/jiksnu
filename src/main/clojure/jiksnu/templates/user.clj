(ns jiksnu.templates.user
  (:use ciste.debug
        closure.templates.core))

(deftemplate show-minimal
  [user]
  {:user user})

(deftemplate edit-form
  [{id :_id
    :keys [username display-name first-name last-name domain
           email bio password confirm-password url location]
    :as user}]
  {:id id
   :username username
   :domain domain
   :location location
   :display-name display-name
   :first-name first-name
   :last-name last-name
   :email email
   :bio bio
   :password password
   :confirm-password confirm-password
   :url url})

