(ns jiksnu.actions.user-actions
  (:use ciste.config
        ciste.core
        ciste.debug
        clj-tigase.core
        [clojure.tools.logging :only (error)]
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
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.User
           tigase.xml.Element))

(declare update)

(defaction add-link
  [user link]
  (if-let [existing-link (model.user/get-link user (:rel link))]
    user
    (entity/update
     User {:_id (:_id user)}
     {:$addToSet
      {:links {:href (:href link)
               :type (:type link)
               :rel (:rel link)}}})))

(defaction create
  [options]
  (let [prepared-user (merge {:discovered false
                              :local false}
                             options)]
    (model.user/create prepared-user)))

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
  (let [domain (model.user/get-domain user)]
    domain))

(defaction find-or-create
  [username domain]
  (let [domain-record (actions.domain/find-or-create domain)]
    (if-let [user (model.user/show username domain)]
      user
      (create {:username username
               :domain domain
               :updated (sugar/date)
               :discovered false}))))

(defaction index
  [options]
  (model.user/index))

(defaction profile
  [& _])

(defaction register
  [options]
  (let [{username :username
         password :password
         confirm-password :confirm-password} options]
    (if (and username password confirm-password)
      ;; Passwords must match
      (if (= password confirm-password)
        (let [user {:username username
                    :domain (:domain (config))
                    :password password
                    :confirm-password password}]
          (create user))))))

(declare update)

(defaction remote-create
  [user options]
  (let [user (merge user
                    {:updated (sugar/date)
                     :discovered true}
                    options)]
    (update user options)))

(defaction remote-profile
  [& _])

(defaction remote-user
  [user]
  user)

(defaction salmon
  [request]
  (spy request))

(defaction show
  ;;   "This action just returns the passed user.
  ;; The user needs to be retreived in the filter."
  [user]
  (model.user/fetch-by-id (:_id user)))

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
    (entity/update
     User {:_id (:_id user)}
     {:$set {:hub hub-link}})
    user))

(defaction update-usermeta
  [user]
  (let [xrd (fetch-user-meta user)
        links (actions.webfinger/get-links xrd)
        new-user (assoc user :links links)]
    (doseq [link links]
      (add-link user link))))
