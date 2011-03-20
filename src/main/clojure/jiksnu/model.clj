(ns jiksnu.model
  (:use ciste.factory
        [jiksnu.config :only (config)]
        karras.entity
        plaza.rdf.core
        plaza.rdf.implementations.jena)
  (:require [karras.core :as karras]
            [karras.sugar :as sugar])
  (:import com.cliqset.abdera.ext.activity.ActivityExtensionFactory
           com.cliqset.abdera.ext.poco.PocoExtensionFactory
           org.apache.abdera.Abdera
           org.apache.abdera.factory.Factory
           org.apache.axiom.util.UIDGenerator
           org.bson.types.ObjectId))

(init-jena-framework)
(register-rdf-ns :dc "http://purl.org/dc/elements/1.1/")
(register-rdf-ns :foaf "http://xmlns.com/foaf/0.1/")


(defonce ^Abdera #^:dynamic *abdera* (Abdera.))
(defonce ^Factory #^:dynamic *abdera-factory* (.getFactory *abdera*))
(defonce #^:dynamic *abdera-parser* (.getParser *abdera*))

(.registerExtension *abdera-factory* (ActivityExtensionFactory.))
(.registerExtension *abdera-factory* (PocoExtensionFactory.))

(def #^:dynamic *mongo-database*
     (karras/mongo-db (-> (config) :database :name)))

(defn mongo-database
  []
  (karras/mongo-db (-> (config) :database :name)))

(defembedded Person
  [:name])

(defentity Activity
  [:id
   :actor
   :title])

(defentity Subscription
  [:to :from :created])

(defentity User
  [:_id
   :username
   :created
   :updated
   :domain
   :subnodes])

(defentity Item
  [:user :activity :created])

(defentity Domain
  [:osw :ostatus])

(defmacro with-database
  [& body]
  `(karras/with-mongo-request (mongo-database)
     ~@body))

(defmacro with-environment
  [environment & body]
  `(binding [jiksnu.config/*current-environment* ~environment]
     (with-database
       ~@body)))

(def activity? (partial instance? Activity))
(defn subscription?
  [s]
  (instance? Subscription s))

(defn make-id
  [id]
  (ObjectId. id))

(defn new-id
  []
  (UIDGenerator/generateURNString))

(defseq :id
  [n]
  n)

(defseq :word
  [n]
  (str "word" n))

(deffactory Activity
  {:_id #'new-id
   :title (fseq :word)
   :summary (fseq :word)
   :published #'sugar/date
   :updated #'sugar/date
   :public true})

(deffactory User
  (let [password (fseq :word)]
    {:username (fseq :word)
     :domain (-> (config) :domain)
     :name (fseq :word)
     :first-name (fseq :word)
     :last-name (fseq :word)
     :password password
     :confirm-password password}))

(deffactory Subscription
  {:to (fseq :word)
   :from (fseq :word)
   :created #'sugar/date})
