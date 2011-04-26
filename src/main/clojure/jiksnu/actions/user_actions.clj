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
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [karras.entity :as entity])
  (:import jiksnu.model.User
           tigase.xml.Element))

(declare update)

(defaction add-link
  [user link]
  (update
   (if-let [existing-link (get-link user (:rel link))]
     (do
       (assoc :links )
       user)
     (entity/update
      User {:_id (:-id user)}
      {:$addToSet {:links (bean link)}}))))

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

(defaction discover
  [^User user]
  user)

(defaction edit
  [& _])

(defaction fetch-remote
  [user]
  (let [domain (:domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction fetch-updates
  [user]
  user)

(declare show)

(defaction find-hub
  [user]
  (let [domain (get-domain user)]
    domain))

(defaction find-or-create
  [username domain]
  (let [domain-record (actions.domain/find-or-create domain)]
    (if-let [user (show username domain)]
      user
      (create {:username username :domain domain}))))

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

(defaction update-hub
  [user]
  (let [feed (fetch-user-feed user)
        hub-link (get-hub-link feed)]
    (add-link user :hub (spy hub-link))))

(defaction update-usermeta
  [user]
  (let [xrd (fetch-user-meta user)
        links (actions.webfinger/get-links xrd)
        new-user (assoc user :links links)]
    (doseq [link links]
      (add-link user link))))
