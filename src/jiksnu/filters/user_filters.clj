(ns jiksnu.filters.user-filters
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.user-actions)
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import tigase.xml.Element))

(deffilter #'create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

;; TODO: this one wasn't working in the first place
(deffilter #'create :xmpp
  [action request]
  (let [{:keys [items]} request]
    (let [properties
          (flatten
           (map helpers.user/process-vcard-element items))]
      (action properties))))

(deffilter #'delete :http
  [action request]
  (-> request :params :id model.user/fetch-by-id action))

(deffilter #'delete :xmpp
  [action request]
  ;; TODO: implement
  )

(deffilter #'discover :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id id)]
    (action user)))

(deffilter #'fetch-remote :xmpp
  [action request]
  (fetch-by-jid (:to request)))

(deffilter #'fetch-updates :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id id)]
    (action user)))

(deffilter #'index :http
  [action request]
  (let [{params :params} request]
    (action params)))


(deffilter #'index :xmpp
  [action request]
  (action {}))

(deffilter #'profile :http
  [action request]
  (if-let [user (session/current-user)]
    user (log/error "no user")))

(deffilter #'register :http
  [action {{:keys [username password confirm-password] :as params} :params}]
  (if (= password confirm-password)
    (action params)
    (throw (RuntimeException. "Password and confirm password do not match"))))

(deffilter #'register-page :http
  [action request]
  (action))

;; TODO: extract vcard->user

;; (deffilter #'remote-create :xmpp
;;   [action request]
;;   (let [{:keys [to from payload]} request
;;         user (fetch-by-jid from)]
;;     (let [vcard (first (element/children payload))

;;           avatar-url-element (abdera/find-children vcard "/vcard/photo/uri")
;;           first-name-element (abdera/find-children vcard "/vcard/n/given/text")
;;           gender-element     (abdera/find-children vcard "/vcard/gender")
;;           last-name-element  (abdera/find-children vcard "/vcard/n/surname/text")
;;           name-element       (abdera/find-children vcard "/vcard/fn/text")
;;           url-element        (abdera/find-children vcard "/vcard/url/uri")

;;           avatar-url (abdera/get-text avatar-url-element)
;;           first-name (abdera/get-text first-name-element)
;;           gender     (abdera/get-text gender-element)
;;           last-name  (abdera/get-text last-name-element)
;;           name       (abdera/get-text name-element)
;;           url        (abdera/get-text url-element)]
;;       (action user {:gender gender
;;                     :name name
;;                     :first-name first-name
;;                     :last-name last-name
;;                     :url url
;;                     :avatar-url avatar-url}))))

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request
        user (show id)]
    (action user)))

;; TODO: This action is working off of a jid
(deffilter #'show :xmpp
  [action request]
  (let [{:keys [to]} request
        user (fetch-by-jid to)]
    (action user)))

(deffilter #'update :http
  [action request]
  (let [{params :params} request
        {username :username} params
        user (show username)]
    (action user params)))

(deffilter #'update-hub :http
  [action request]
  (let [{params :params} request
        {username :id} params
        user (model.user/fetch-by-id username)]
    (action user)))

(deffilter #'update-profile :http
  [action {params :params}]
  (action params))

(deffilter #'xmpp-service-unavailable :xmpp
  [action request]
  (let [from (:from request)
        user (find-or-create-by-jid from)]
    (action user)))

(deffilter #'user-meta :http
  [action request]
  (->> request :params :uri
       model.user/split-uri
       (apply model.user/get-user)
       action))
