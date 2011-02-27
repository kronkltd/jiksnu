(ns jiksnu.atom.view.user-view
  (:use ciste.view
        jiksnu.view
        jiksnu.model
        jiksnu.namespace)
  (:require [jiksnu.model.user :as model.user])
  (:import javax.xml.namespace.QName
           com.cliqset.abdera.ext.activity.object.Person
           jiksnu.model.User
           java.net.URI
           org.apache.abdera.model.Entry))

(defn make-object
  [namespace name prefix]
  (com.cliqset.abdera.ext.activity.Object.
   *abdera-factory* (QName. namespace name prefix)))

(defsection show-section [User :atom]
  [^User user & options]
  (let [person (Person. (make-object atom-ns "author" ""))
        author-uri (full-uri user)]
    (.setObjectType person person-uri)
    (.setId person (str "acct:" (:username user)
                        "@" (:domain user)))
    (.setName person (:first-name user) (:last-name user))
    (.setDisplayName person (:name user))
    (.addSimpleExtension person atom-ns "email" ""
                         (or (:email user) author-jid))
    (.addSimpleExtension person atom-ns "name" "" (:name user))
    (.addAvatar person (:avatar-url user) "image/jpeg")
    (.addSimpleExtension person atom-ns "uri" "" author-uri)
    person))

(defn get-uri
  [^User user]
  (str (:_id user) "@" (:domain user)))

(defn ^URI author-uri
  [^Entry entry]
  (let [author (.getAuthor entry)]
    (let [uri (.getUri author)]
      (URI. (.toString uri)))))
