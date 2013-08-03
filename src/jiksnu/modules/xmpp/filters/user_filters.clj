(ns jiksnu.modules.xmpp.filters.user-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.user-actions
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]))

(deffilter #'delete :xmpp
  [action request]
  (cm/implement))

;; fetch-remote

(deffilter #'fetch-remote :xmpp
  [action request]
  (model.user/fetch-by-jid (:to request)))

(deffilter #'index :xmpp
  [action request]
  (action {}))

;; TODO: This action is working off of a jid
(deffilter #'show :xmpp
  [action request]
  (let [{:keys [to]} request
        user (model.user/fetch-by-jid to)]
    (action user)))

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
;;                     :avatarUrl avatar-url}))))

