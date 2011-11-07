(ns jiksnu.model
  (:use (ciste core
               [config :only [config definitializer environment load-config]]
               [debug :only [spy]]
               sections)
        ciste.sections.default
        (clj-factory [core :only [factory]])
        (karras [core :only [MongoMappable]]
                [entity :only [defembedded defentity delete-all]])
        plaza.rdf.core
        plaza.rdf.implementations.jena)
  (:require (aleph [formats :as f]
                   [http :as h])
            (clj-http [client :as client])
            (clojure [xml :as xml]
                     [zip :as zip])
            (clojure.data [json :as json])
            (clojure.data.zip [xml :as xf])
            (jiksnu [namespace :as ns])
            (karras [core :as karras]
                    [sugar :as sugar])
            (lamina [core :as l]))
  (:import java.io.InputStream
           java.io.PrintWriter
           java.io.StringReader
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId
           org.joda.time.DateTime
           org.dom4j.DocumentFactory
           org.dom4j.io.SAXReader
           org.xml.sax.InputSource))

(def ^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ssZ")

(def ^:dynamic *mongo-database* (ref nil))
(defonce xml-reader (SAXReader.))
(defonce ^:dynamic *formatter*
  (SimpleDateFormat. *date-format*))

(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defn format-date
  [^Date date]
  (condp = (class date)
    String (DateTime/parse date)
    DateTime date
    Date (.format (SimpleDateFormat. *date-format*) date)
    date))

(init-jena-framework)
;; TODO: Find a better ns for this
(register-rdf-ns :dc ns/dc)
(register-rdf-ns :foaf ns/foaf)

(defn force-coll
  [x]
  (if (coll? x)
    x (list x)))

(defn mongo-database
  []
  (karras/mongo-db (config :database :name)))

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

(defn activity?
  [activity]
  (instance? Activity activity))

(defn domain?
  [domain]
  (instance? Domain domain))

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
  (if-let [response (try (client/get url) (catch Exception ex))]
    (let [{:keys [body status]} response]
      (when (not (#{404 500} status))
        body))))

(defn fetch-document
  [url]
  (.read xml-reader (URL. url)))

(defn drop-all!
  []
  (doseq [entity [Activity Like Subscription
                  User Item Domain PushSubscription
                  MagicKeyPair]]
    (delete-all entity)))

(defn set-database!
  []
  (alter-var-root #'karras/*mongo-db* (fn [_] (mongo-database))))

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

(extend-type DateTime
   MongoMappable
   (to-dbo [d] (.toDate d))
   (to-clj [d] (DateTime/parse d))
   (to-description [d] (str d)))

(definitializer
  (load-config)
  (set-database!))
