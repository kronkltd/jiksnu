(ns jiksnu.xmpp.controller.user-controller
  (:use jiksnu.namespace
        [jiksnu.session :only (current-user)]
        jiksnu.xmpp.view)
  (:require [jiksnu.model.user :as model.user])
  (:import tigase.xml.Element))

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

;; (defn index
;;   [request]
;;   (model.user/index))

(defn show
  [request]
  (let [to (:to request)
        id (.getLocalpart to)
        domain (.getDomain to)]
    (println "id: " id)
    (model.user/show id domain)))

(defn create
  [request]
  (let [vcard-elements (:items request)]
    (doseq [property
            (flatten
             (map process-vcard-element
                  vcard-elements))]
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
