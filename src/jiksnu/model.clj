(ns jiksnu.model
  (:use (ciste core
               [config :only (config environment)]
               sections)
        ciste.sections.default
        clj-factory.core
        karras.entity
        plaza.rdf.core
        plaza.rdf.implementations.jena)
  (:require (clojure [xml :as xml]
                     [zip :as zip])
            (clojure.data [json :as json])
            (jiksnu [namespace :as namespace]
                    [redis :as redis])
            (karras [core :as karras]
                    [sugar :as sugar]))
  (:import java.io.PrintWriter
           java.io.StringReader
           java.text.SimpleDateFormat
           java.util.Date
           org.bson.types.ObjectId
           org.xml.sax.InputSource))

(def ^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ssZ")

(def ^:dynamic *mongo-database* (ref nil))

(defonce ^:dynamic *formatter*
  (SimpleDateFormat. *date-format*))

(defn format-date
  [^Date date]
  (.format *formatter* date))

(init-jena-framework)
;; TODO: Find a better ns for this
(register-rdf-ns :dc namespace/dc-ns)
(register-rdf-ns :foaf namespace/foaf-ns)

(defn mongo-database*
  []
  (karras/mongo-db (config :database :name)))

(defn mongo-database
  []
  (or @*mongo-database*
      (mongo-database*)))

(defembedded Person
  [:name])

(defentity Activity
  [:id
   :actor
   :title])

(defentity Like
  [:id :user :activity :created])

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

(defentity PushSubscription
  [:hub :verify-token :secret :created :updated :topic])

(defentity MagicKeyPair
  [:crt-coefficient :prime-exponent-p :prime-exponent-q
   :prime-p :prime-q :public-exponent :private-exponent :userid])

(defmacro with-database
  [& body]
  `(karras/with-mongo-request (mongo-database)
    ~@body))

(defmacro with-environment
  [environment & body]
  `(binding [ciste.config/*environment* ~environment]
     (redis/init-client)
     (with-database
       ~@body)))

(def activity? (partial instance? Activity))
(def subscription? (partial instance? Subscription))
(def user? (partial instance? User))

(defn make-id
  [^String id]
  (ObjectId. id))

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

(defn parse-str
  [s]
  (-> s StringReader. InputSource.
      xml/parse zip/xml-zip))

;; TODO: Find a good place for this

(defn write-json-date
  ([date out]
     (write-json-date date out false))
  ([^Date date ^PrintWriter out escape-unicode?]
     (let [formatted-date (.format (SimpleDateFormat. *date-format*) date)]
       (.print out (str "\"" formatted-date "\"")))))

(defn write-json-object-id
  ([id out]
     (write-json-object-id id out false))
  ([id ^PrintWriter out escape-unicode]
     (.print out (str "\"" id "\""))))

(extend Date json/Write-JSON
  {:write-json write-json-date})
(extend ObjectId json/Write-JSON
  {:write-json write-json-object-id})

(load-file "factories.clj")
