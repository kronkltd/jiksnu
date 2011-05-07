(ns jiksnu.filters.user-filters
  (:use clj-tigase.core
        [clojure.contrib.logging :only (error)]
        ciste.config
        ciste.debug
        ciste.filters
        jiksnu.abdera
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers
        jiksnu.session
        jiksnu.view)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import tigase.xml.Element))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
           (map process-vcard-element items))]
      (action properties))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'delete :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

(deffilter #'delete :xmpp
  [action request]
  ;; TODO: implement
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; discover
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'discover :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id id)]
    (action user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'edit :http
  [action request]
  (let [user (show request)]
    user))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'fetch-remote :xmpp
  [action request]
  (model.user/fetch-by-jid (:to request)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-updates
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'fetch-updates :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id id)]
    (action user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; inbox
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (deffilter inbox :xmpp
;;   [request]
;;   ;; TODO: limit this to the inbox of the user
;;   (model.user/inbox))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [action request]
  (let [{params :params} request]
    (action params)))

(deffilter #'index :xmpp
  [action request]
  '()
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'profile :http
  [action request]
  (if-let [user (current-user)]
    user (error "no user")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; register
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'register :http
  [action request]
  true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-create :xmpp
  [action request]
  (let [{:keys [to from payload]} request
        user (model.user/fetch-by-jid from)]
    (let [vcard (first (children payload))

          avatar-url-element (find-children vcard "/vcard/photo/uri")
          first-name-element (find-children vcard "/vcard/n/given/text")
          gender-element     (find-children vcard "/vcard/gender")
          last-name-element  (find-children vcard "/vcard/n/surname/text")
          name-element       (find-children vcard "/vcard/fn/text")
          url-element        (find-children vcard "/vcard/url/uri")

          avatar-url (get-text avatar-url-element)
          first-name (get-text first-name-element)
          gender     (get-text gender-element)
          last-name  (get-text last-name-element)
          name       (get-text name-element)
          url        (get-text url-element)]
      (action user {:gender gender
                    :name name
                    :first-name first-name
                    :last-name last-name
                    :url url
                    :avatar-url avatar-url}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-profile :http
  [action request]
  (let [{{id :id} :params} request]
    (let [user (model.user/fetch-by-id id)]
      user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-user
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-user :http
  [action request]
  (let [{{uri :*} :params} request]
    (action (model.user/fetch-by-uri uri))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/show id)]
    (action user)))

;; TODO: This action is working off of a jid
(deffilter #'show :xmpp
  [action request]
  (let [{:keys [to]} request
        user (model.user/fetch-by-jid to)]
    (action user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; update
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'update :http
  [action request]
  (let [{params :params} request
        {username :username} params
        user (model.user/show username)]
    (action user params)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; update
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'update-hub :http
  [action request]
  (let [{params :params} request
        {username :id} params
        user (model.user/fetch-by-id username)]
    (action user)))
