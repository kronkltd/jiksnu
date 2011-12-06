(ns jiksnu.templates.user
  (:use (ciste [debug :only [spy]])
        (closure.templates [core :only [deftemplate]])
        (jiksnu [session :only [current-user]]))
  (:require (jiksnu.model [user :as model.user])))

(deftemplate show
  [user]
  (let [data (model.user/format-data user)]
    (if-let [current (current-user)]
      (assoc data :authenticated (model.user/format-data current))
      data)))

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
  {:users (map model.user/format-data users)})
