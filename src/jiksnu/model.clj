(ns jiksnu.model
  (:use (ciste core
               [config :only [config definitializer environment load-config]]
               [debug :only [spy]])
        (clj-factory [core :only [factory]])
        (clojure.core [incubator :only [-?>]])
        (karras [core :only [MongoMappable]]
                [entity :only [defembedded defentity delete-all]]))
  (:require (clj-http [client :as client])
            (clojure [string :as string]
                     [xml :as xml]
                     [zip :as zip])
            (clojure.data [json :as json])
            (clojure.tools [logging :as log])
            (jiksnu [namespace :as ns])
            (karras [core :as karras]
                    [sugar :as sugar])
            (lamina [core :as l])
            (net.cgrand [enlive-html :as enlive])
            (plaza.rdf [core :as rdf])
            (plaza.rdf.implementations [jena :as jena]))
  (:import java.io.InputStream
           java.io.FileNotFoundException
           java.io.PrintWriter
           java.io.StringReader
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           nu.xom.Builder
           nu.xom.Document
           nu.xom.Node
           org.bson.types.ObjectId
           org.joda.time.DateTime
           org.xml.sax.InputSource))

(def ^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ssZ")

(def ^:dynamic *mongo-database* (ref nil))

(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defn format-date
  [^Date date]
  (condp = (class date)
    String (DateTime/parse date)
    DateTime date
    Date (.format (SimpleDateFormat. *date-format*) date)
    date))

(defn strip-namespaces
  [val]
  (-?> val
       (string/replace #"http://activitystrea.ms/schema/1.0/" "")
       (string/replace #"http://ostatus.org/schema/1.0/" "")))

(defn date->twitter
  [date]
  (let [formatter (SimpleDateFormat. "EEE MMM d HH:mm:ss Z yyyy")]
    (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
    (.format formatter date)))

;; TODO: I'm sure this exists somewhere else
(defn make-subscribe-uri
  [url options]
  (str url "?"
       (string/join
        "&"
        (map
         (fn [[k v]] (str (name k) "=" v))
         options))))





(jena/init-jena-framework)
;; TODO: Find a better ns for this
(rdf/register-rdf-ns :dc ns/dc)
(rdf/register-rdf-ns :foaf ns/foaf)
(rdf/register-rdf-ns :sioc ns/sioc)
(rdf/register-rdf-ns :cert ns/cert)

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

(defentity FeedSource
  [:hub :verify-token :secret :created :updated :topic])

(defentity FeedSubscription
  [:hub :verify-token :secret :created :updated :topic])

(defentity MagicKeyPair
  [:userid

   :crt-coefficient
   :armored-crt-coefficient

   :prime-exponent-p
   :armored-prime-exponent-p
   
   :prime-exponent-q
   :armored-prime-exponent-q

   :prime-p
   :armored-prime-p
   
   :prime-q
   :armored-prime-q

   :public-exponent
   :armored-public-exponent

   :private-exponent
   :armored-private-exponent
   ])

(defentity Group
  [:name])

;; Entity predicates

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

(defn drop-all!
  "Drop all collections"
  []
  (doseq [entity [Activity Like Subscription
                  User Item Domain FeedSource
                  MagicKeyPair]]
    (delete-all entity)))







(defn ^ObjectId make-id
  "Create an object id from the provided string"
  [^String id]
  (ObjectId. id))

(defn string->zip
  "parse xml string as a zipper sequence"
  [^String xml]
  (-> xml StringReader. InputSource.
      xml/parse zip/xml-zip))

(defn ^Document stream->document
  "Read an input stream into a XOM document"
  [^InputStream input-stream]
  (.build (Builder.) input-stream))

(defn ^Document string->document
  "Read a string into a XOM document"
  [^String xml]
  (.build (Builder.) xml ""))

(defn ^String fetch-resource
  "Fetch the url, return the string"
  [^String url]
  (log/infof "fetching resource: %s" url)
  (if-let [response (try (client/get url) (catch Exception ex))]
    (let [{:keys [body status]} response]
      (when (not (#{404 500} status))
        body))))

(defn ^Document fetch-document
  "Fetch the url and return it as a XOM document"
  [^String url]
  (-?> url fetch-resource string->document))


;; TODO: reverse order
(defn query
  "Return the sequence of nodes that match the xpath expression"
  [^String path ^Node doc]
  (let [nodes (.query doc path)]
    (map
     #(.get nodes %)
     (range (.size nodes)))))


(defn extract-atom-link
  "Find the atom link in the page identified by url"
  [url]
  (-> url
      fetch-resource
      StringReader.
      enlive/html-resource
      (enlive/select [:link])
      (->> (filter #(= "alternate" (:rel (:attrs %))))
           (filter #(= "application/atom+xml" (:type (:attrs %))))
           (map #(-> % :attrs :href)))
      first))


;; Database functions

(defn set-database!
  "Set the connection for mongo"
  []
  (log/debug (str "setting database for " (environment)))
  (alter-var-root #'karras/*mongo-db* (fn [_] (mongo-database))))

;; TODO: Find a good place for this

(defn write-json-date
  ([^Date date ^PrintWriter out]
     (write-json-date date out false))
  ([^Date date ^PrintWriter out escape-unicode?]
     (let [formatted-date (.format (SimpleDateFormat. *date-format*) date)]
       (.print out (str "\"" formatted-date "\"")))))

(defn write-json-object-id
  ([id ^PrintWriter out]
     (write-json-object-id id out false))
  ([id ^PrintWriter out escape-unicode]
     (.print out (str "\"" id "\""))))

(extend Date json/Write-JSON
        {:write-json write-json-date})
(extend ObjectId json/Write-JSON
        {:write-json write-json-object-id})

(extend-type DateTime
  MongoMappable
  (to-dbo [d] (.toDate d))
  (to-clj [d] (DateTime/parse d))
  (to-description [d] (str d)))

;; Factory specific support in Ciste?
;; (load-file "factories.clj")

(definitializer
  (set-database!))

