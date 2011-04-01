(ns jiksnu.xmpp.controller.user-controller
  (:use ciste.config
        ciste.debug
        clj-tigase.core
        clojure.contrib.logging
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user)]
        jiksnu.xmpp.element
        jiksnu.xmpp.view)
  (:require [jiksnu.model.user :as model.user])
  (:import tigase.xml.Element))

;; xmpp

(defn rule-element?
  [^Element element]
  (= (.getName element) "acl-rule"))

(defn rule-map
  [rule]
  (let [^Element action-element (.getChild rule "acl-action")
        ^Element subject-element (.getChild rule "acl-subject")]
    {:subject (.getAttribute subject-element "type")
     :permission (.getAttribute action-element "permission")
     :action (.getCData action-element)}))

(defn property-map
  [user property]
  (let [child-elements (children property)
        rule-elements (filter rule-element? child-elements)
        type-element (first (filter (comp not rule-element?) child-elements))]
    {:key (.getName property)
     :type (.getName type-element)
     :value (.getCData type-element)
     :rules (map rule-map rule-elements)
     :user user}))

(defn process-vcard-element
  [element]
  (fn [vcard-element]
    (map (partial property-map (current-user))
         (children vcard-element))))

(defn show
  [request]
  (let [to (:to request)]
    (model.user/fetch-by-jid to)))

(defn create
  [request]
  (let [{:keys [items]} request]
    (doseq [property
            (flatten
             (map process-vcard-element items))]
      (model.user/create property))))

(defn delete
  [request]
  ;; TODO: implement
  '())

;; (defn index
;;   [request]
;;   '()
;;   )

(defn inbox
  [request]
  ;; TODO: limit this to the inbox of the user
  (model.user/inbox))

(defn fetch-remote
  [request]
  (model.user/fetch-by-jid (:to request)))

(defn remote-create
  [request]
  (let [{:keys [to from payload]} request
        user (model.user/fetch-by-jid from)
        vcard (first (children payload))
        gender (.getCData (find-children vcard "/vcard/gender"))
        name (.getCData (find-children vcard "/vcard/fn/text"))
        first-name (.getCData (find-children vcard "/vcard/n/given/text"))
        last-name (.getCData (find-children vcard "/vcard/n/surname/text"))
        url (.getCData (find-children vcard "/vcard/url/uri"))
        avatar-url (.getCData (find-children vcard "/vcard/photo/uri"))
        new-user {:gender gender
                  :name name
                  :first-name first-name
                  :last-name last-name
                  :url url
                  :avatar-url avatar-url}
        merged-user (merge user new-user)]
    (jiksnu.model.user/update merged-user)))

;; http

(defn index
  [request]
  (model.user/index))

(defn show
  [{{id "id"} :params
    :as request}]
  (model.user/show id))

(defn delete
  [{{id "id"} :params}]
  (model.user/delete id)
  true)

(defn create
  [{{username "username"
     password "password"
     confirm-password "confirm_password"} :params
     :as request}]
  (if (and username password confirm-password)
    (if (= password confirm-password)
      (do
        (model.user/create {:username username
                            :domain (:domain (config))
                            :password password
                            :confirm_password password})))))

(defn update
  [{{username "username" :as params} :params :as request}]
  (let [user (model.user/show username (:domain (config)))]
    (let [new-params
          (-> (into {}
                    (map
                     (fn [[k v]]
                       (if (not= v "")
                         [(keyword k) v]))
                     params))
              (dissoc :id)
              #_(assoc :_id id))]
      (model.user/update new-params))))

(defn edit
  [request]
  (let [user (show request)]
    user))

(defn register
  [request]
  true)

(defn profile
  [request]
  (if-let [user (current-user)]
    user (error "no user")))

(defn remote-profile
  [request]
  (let [{{id "id"} :params} request]
    (let [user (model.user/fetch-by-id id)]
      user)))

