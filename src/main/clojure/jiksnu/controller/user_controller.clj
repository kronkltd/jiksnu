(ns jiksnu.controller.user-controller
  (:use ciste.config
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

(defaction show
  [id]
  (model.user/show id)
  )

(defaction create
  [options]
  (let [{username "username"
         password "password"
         confirm-password "confirm_password"}]
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

(defaction index
  [options]
  (model.user/index))



;; xmpp





(deffilter inbox :xmpp
  [request]
  ;; TODO: limit this to the inbox of the user
  (model.user/inbox))





