(ns jiksnu.actions.user-actions
  (:use ciste.config
        ciste.core
        ciste.debug
        clj-tigase.core
        [clojure.contrib.logging :only (error)]
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user)]
        jiksnu.view
        jiksnu.xmpp.element)
  (:require [jiksnu.model.user :as model.user])
  (:import tigase.xml.Element))

(defaction create
  [options]
  (let [{username "username"
         password "password"
         confirm-password "confirm_password"} options]
    (if (and username password confirm-password)
      (if (= password confirm-password)
        (do
          (model.user/create {:username username
                              :domain (:domain (config))
                              :password password
                              :confirm_password password}))))))

(defaction delete
  [id]
  (model.user/delete id))

(defaction edit
  [& _])

(defaction fetch-remote
  [& _])

(defaction index
  [options]
  (model.user/index))

(defaction profile
  [& _])

(defaction register
  [& _])

(defaction remote-create
  [& _])

(defaction remote-profile
  [& _])

(defaction show
;;   "This action just returns the passed user.
;; The user needs to be retreived in the filter."
  [user]
  #_(model.user/fetch-by-id id)
  user)

(defaction update
  [& _])
