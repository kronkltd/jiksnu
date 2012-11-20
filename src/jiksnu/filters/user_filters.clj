(ns jiksnu.filters.user-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.user-actions
        [jiksnu.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]))

;; create

(deffilter #'create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

;; delete

(deffilter #'delete :command
  [action id]
  (when-let [item (model.user/fetch-by-id (model/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action request]
  (let [id (:id (:params request))]
    (when-let [item (model.user/fetch-by-id (model/make-id id))]
      (action item))))

(deffilter #'delete :xmpp
  [action request]
  (cm/implement))

;; discover

(deffilter #'discover :command
  [action id]
  (if-let [item (model.user/fetch-by-id (model/make-id id))]
    (action item)))

(deffilter #'discover :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id (model/make-id id))]
    (action user)))

;; fetch-remote

(deffilter #'fetch-remote :xmpp
  [action request]
  (model.user/fetch-by-jid (:to request)))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

(deffilter #'index :xmpp
  [action request]
  (action {}))

;; profile

(deffilter #'profile :http
  [action request]
  (if-let [user (session/current-user)]
    user (log/error "no user")))

;; register

(deffilter #'register :http
  [action {{:keys [username password confirm-password] :as params} :params}]
  (if (:accepted params)
    (if (= password confirm-password)
      (action params)
      (throw+ "Password and confirm password do not match"))
    (throw+ "you didn't check the box")))

;; register-page

(deffilter #'register-page :http
  [action request]
  (action))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id (model/make-id id))]
     (action user))))

;; TODO: This action is working off of a jid
(deffilter #'show :xmpp
  [action request]
  (let [{:keys [to]} request
        user (model.user/fetch-by-jid to)]
    (action user)))

;; update

(deffilter #'update :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id (model/make-id id))]
     (action user))))

(deffilter #'update :command
  [action id]
  (let [item (model.user/fetch-by-id (model/make-id id))]
    (action item)))

;; update-profile

(deffilter #'update-profile :http
  [action {params :params}]
  (action params))

;; user-meta

(deffilter #'user-meta :http
  [action request]
  (->> request :params :uri
       model.user/split-uri
       (apply model.user/get-user)
       action))

;; xmpp-service-unavailable

(deffilter #'xmpp-service-unavailable :xmpp
  [action request]
  (let [from (:from request)
        user (find-or-create-by-jid from)]
    (action user)))




;; TODO: extract vcard->user

;; (deffilter #'remote-create :xmpp
;;   [action request]
;;   (let [{:keys [to from payload]} request
;;         user (model.user/fetch-by-jid from)]
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

;; (deffilter #'update-hub :http
;;   [action request]
;;   (let [{params :params} request
;;         {username :id} params
;;         user (model.user/fetch-by-id username)]
;;     (action user)))

