(ns jiksnu.model
  (:use (ciste core
               [config :only (config environment)]
               sections)
        ciste.sections.default
        clj-factory.core
        (karras [entity :only (defembedded
                                defentity delete-all)])
        plaza.rdf.core
        plaza.rdf.implementations.jena)
  (:require (aleph [formats :as f]
                   [http :as h])
            (clojure [xml :as xml]
                     [zip :as zip])
            (clojure.data [json :as json])
            (clojure.data.zip [xml :as xf])
            (jiksnu [namespace :as ns])
            (karras [core :as karras]
                    [sugar :as sugar]))
  (:import java.io.InputStream
           java.io.PrintWriter
           java.io.StringReader
           java.text.SimpleDateFormat
           java.util.Date
           org.bson.types.ObjectId
           org.dom4j.DocumentFactory
           org.dom4j.io.SAXReader
           org.xml.sax.InputSource))

(def ^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ssZ")

(def ^:dynamic *mongo-database* (ref nil))
(defonce xml-reader (SAXReader.))
(defonce ^:dynamic *formatter*
  (SimpleDateFormat. *date-format*))

(defn format-date
  [^Date date]
  (if date (.format *formatter* date)))

(init-jena-framework)
;; TODO: Find a better ns for this
(register-rdf-ns :dc namespace/dc)
(register-rdf-ns :foaf namespace/foaf)

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

;; (defmacro with-environment
;;   [environment & body]
;;   `(binding [ciste.config/*environment* ~environment]
;;      (redis/init-client)
;;      (with-database
;;        ~@body)))

(defn activity?
  [activity]
  (instance? Activity activity))

(defn subscription?
  [subscription]
  (instance? Subscription subscription))

(defn user?
  [user] (instance? User user))

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

(defn str->zip
  [s]
  (zip/xml-zip (parse-str s)))

(defn stream->document
  [^InputStream input-stream]
  (.read xml-reader input-stream))

(defn fetch-resource
  [url]
  (-> {:method :get
       :url url}
      h/sync-http-request
      :body f/channel-buffer->string))

(defn drop-all!
  []
  (doseq [entity [Activity Like Subscription
                  User Item Domain PushSubscription
                  MagicKeyPair]]
    (delete-all entity)))

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
