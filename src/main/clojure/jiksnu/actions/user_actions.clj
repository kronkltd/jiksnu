(ns jiksnu.actions.user-actions
  (:use ciste.config
        ciste.core
        ciste.debug
        clj-tigase.core
        [clojure.contrib.logging :only (error)]
        jiksnu.helpers.user-helpers
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user)]
        jiksnu.view
        jiksnu.xmpp.element)
  (:require [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user])
  (:import tigase.xml.Element))

(defaction create
  [options]
  (let [{username :username
         password :password
         confirm-password :confirm-password} options]
    (if (and username password confirm-password)
      (if (= password confirm-password)
        (let [opts {:username username
                    :domain (:domain (config))
                    :password password
                    :confirm-password password}]
          (model.user/create opts))))))

(defaction delete
  [id]
  (model.user/delete id))

(defaction edit
  [& _])

(defaction fetch-remote
  [user]
  (let [domain (:domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

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
  [user params]
  (let [new-params
        (-> (into user
                  (map
                   (fn [[k v]]
                     (if (not= v "")
                       [(keyword k) v]))
                   params))
            (dissoc :id))]
   (model.user/update new-params)))

(defaction find-or-create
  [username domain]
  (let [domain-record (actions.domain/find-or-create domain)]
    (if-let [user (show username domain)]
      user
      (create {:username username :domain domain}))))

(defaction discover
  [^User user]
  user)

(defaction fetch-updates
  [user]
  (let [domain (model.domain/show (:domain user))]
    [user
     (if (:xmpp domain)
       (do
         
         [])
       (load-activities user))]))
