(ns jiksnu.atom.view.user-view
  (:use ciste.view
        jiksnu.view
        jiksnu.model)
  (:require [jiksnu.model.user :as model.user])
  (:import javax.xml.namespace.QName
           com.cliqset.abdera.ext.activity.object.Person
           jiksnu.model.User
           java.net.URI
           org.apache.abdera.model.Entry))

(defn make-object
  [name]
  (com.cliqset.abdera.ext.activity.Object.
   *abdera-factory* (QName. name)))

;; (defn ^Person show-section
;;   [^User user]
;;   (let [person (Person. (make-object "author"))]
;;     (.setDisplayName person (:name user))
;;     #_(.setName person (:first-name user) (:last-name user))
;;     (.setObjectType person "person")
;;     (.addAvatar person (:avatar-url user) "application/jpeg")
;;     person))

(defn get-uri
  [^User user]
  (str (:_id user) "@" (:domain user)))

(defn ^URI author-uri
  [^Entry entry]
  (let [author (.getAuthor entry)]
    (let [uri (.getUri author)]
      (URI. (.toString uri)))))
