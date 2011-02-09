(ns jiksnu.model
  (:use [jiksnu.config :only (config)]
        karras.entity)
  (:require [karras.core :as karras])
  (:import org.apache.abdera.Abdera
           org.apache.abdera.factory.Factory
           com.cliqset.abdera.ext.activity.ActivityExtensionFactory))

(defonce ^Abdera #^:dynamic *abdera* (Abdera.))
(defonce ^Factory #^:dynamic *abdera-factory* (.getFactory *abdera*))
(defonce #^:dynamic *abdera-parser* (.getParser *abdera*))
(.registerExtension *abdera-factory* (ActivityExtensionFactory.))

(def #^:dynamic *mongo-database*
     (karras/mongo-db (-> (config) :database :name)))

(defembedded Person
  [:name])

(defentity Activity
  [:id
   :streamFaviconUrl
   :postedTime
   :actor
   :verb
   :object
   :target
   :generator
   :title
   :body
   :privacy])

(defentity Subscription
  [:to :from :created])

(defentity User
  [:_id :domain :subnodes])

(defmacro with-database
  [& body]
  `(karras/with-mongo-request *mongo-database*
     ~@body))

(def activity? (partial instance? Activity))
(def subscription? (partial instance? Subscription))
