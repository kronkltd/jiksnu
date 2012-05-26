(ns jiksnu.helpers.user-helpers
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        ciste.sections
        [clojure.core.incubator :only [-?>]]
        jiksnu.model
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.string :as string]
            [jiksnu.abdera :as abdera]
            [jiksnu.namespace :as namespace]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.webfinger :as model.webfinger]
            [karras.sugar :as sugar])
  (:import javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera2.model.Entry
           tigase.xml.Element))

(defn feed-link-uri
  [^User user]
  (if-let [link (or (model.user/get-link user namespace/updates-from "application/atom+xml")
                    (model.user/get-link user namespace/updates-from nil))]
    (:href link)))

(defn fetch-user-feed
  "returns a feed"
  [^User user]
  (-?> user
       feed-link-uri
       abdera/fetch-feed))

(defn rule-map
  [^Element rule]
  (let [^Element action-element (.getChild rule "acl-action")
        ^Element subject-element (.getChild rule "acl-subject")]
    {:subject (.getAttribute subject-element "type")
     :permission (.getAttribute action-element "permission")
     :action (.getCData action-element)}))

(defn property-map
  [user ^Element property]
  (let [child-elements (element/children property)
        rule-elements (filter abdera/rule-element? child-elements)
        type-element (first (filter (comp not abdera/rule-element?)
                                    child-elements))]
    {:key (.getName property)
     :type (.getName type-element)
     :value (.getCData type-element)
     :rules (map rule-map rule-elements)
     :user user}))

(defn process-vcard-element
  [^Element element]
  (fn [vcard-element]
    (map (partial property-map (current-user))
         (element/children vcard-element))))

(defn vcard-request
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :get}))

(defn fetch-user-meta
  "returns a user meta document"
  [^User user]
  (if-let [uri (model.user/user-meta-uri user)]
    (model.webfinger/fetch-host-meta uri)
    (throw (RuntimeException. "Could not determine user-meta link"))))
