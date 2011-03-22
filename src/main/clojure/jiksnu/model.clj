(ns jiksnu.model
  (:use [ciste.config :only (config)]
        ciste.factory
        clojure.contrib.json
        jiksnu.namespace
        karras.entity
        plaza.rdf.core
        plaza.rdf.implementations.jena)
  (:require [karras.core :as karras]
            [karras.sugar :as sugar])
  (:import com.cliqset.abdera.ext.activity.ActivityExtensionFactory
           com.cliqset.abdera.ext.poco.PocoExtensionFactory
           java.text.SimpleDateFormat
           java.util.Date
           org.apache.abdera.Abdera
           org.apache.abdera.factory.Factory
           org.apache.axiom.util.UIDGenerator
           org.bson.types.ObjectId))

(def #^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ssZ")

(defonce ^Abdera #^:dynamic *abdera* (Abdera.))
(defonce ^Factory #^:dynamic *abdera-factory* (.getFactory *abdera*))
(defonce #^:dynamic *abdera-parser* (.getParser *abdera*))

(def #^:dynamic *mongo-database*
     (karras/mongo-db (-> (config) :database :name)))

(defonce *formatter*
  (SimpleDateFormat. *date-format*))

(defn format-date
  [date]
  (.format *formatter* date))

(init-jena-framework)
(register-rdf-ns :dc dc-ns)
(register-rdf-ns :foaf foaf-ns)

(.registerExtension *abdera-factory* (ActivityExtensionFactory.))
(.registerExtension *abdera-factory* (PocoExtensionFactory.))

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
  `(binding [ciste.config/*current-environment* ~environment]
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

(defn write-json-date [date out escape-unicode?]
  (let [formatted-date (.format (SimpleDateFormat. *date-format*) date)]
    (.print out (str "\"" formatted-date "\""))))

(defn write-json-object-id
  [id out escape-unicode]
  (.print out (str "\"" id "\"")))

(extend Date Write-JSON
  {:write-json write-json-date})
(extend ObjectId Write-JSON
  {:write-json write-json-object-id})
