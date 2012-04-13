(ns jiksnu.model
  (:use (ciste core
               [config :only [config definitializer environment load-config]]
               [debug :only [spy]])
        (clj-factory [core :only [factory]])
        (clojure.core [incubator :only [-?>]])
        (karras [core :only [MongoMappable]]
                [entity :only [defembedded defentity delete-all]]))
  (:require (ciste [model :as cm])
            (clojure [string :as string]
                     [xml :as xml]
                     [zip :as zip])
            (clojure.data [json :as json])
            (clojure.tools [logging :as log])
            (jiksnu [namespace :as ns])
            (karras [core :as karras]
                    [entity :as entity]
                    [sugar :as sugar])
            (lamina [core :as l])
            (net.cgrand [enlive-html :as enlive])
            (plaza.rdf [core :as rdf])
            (plaza.rdf.implementations [jena :as jena]))
  (:import java.io.FileNotFoundException
           java.io.PrintWriter
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId
           org.joda.time.DateTime
           java.io.StringReader
           ))

(def ^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ss'Z'")

(def ^:dynamic *mongo-database* (ref nil))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defn format-date
  "This is a dirty little function to get a properly formatted date."
  ;; TODO: Get more control of passed dates
  [^Date date]
  (condp = (class date)
    String (DateTime/parse date)
    DateTime date
    Date (let [formatter (SimpleDateFormat. *date-format*)]
           (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
           (.format formatter  date))
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
;; all this is really doing is assembling a uri
(defn make-subscribe-uri
  [url options]
  (str url "?"
       (string/join
        "&"
        (map
         (fn [[k v]] (str (name k) "=" v))
         options))))

(defn with-subject
  "Inserts the subject into the first position in the sequence of vectors"
  [s pairs]
  (map (fn [[p o]] [s p o]) pairs))

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

(defentity AuthenticationMechanism
  [:type])

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

(defn parse-http-link
  [url]
  (let [[_ href remainder] (re-matches #"<([^>]+)>; (.*)" url)]
    (->> (string/split remainder #"; ")
         (map #(let [[_ k v] (re-matches #"(.*)=\"(.*)\"" %)] [k v]))
         (into {})
         (merge {"href" href}))))

(defn ^ObjectId make-id
  "Create an object id from the provided string"
  ([] (ObjectId.))
  ([^String id] (ObjectId. id)))

;; this could be more generic
(defn extract-atom-link
  "Find the atom link in the page identified by url"
  [url]
  (->> url
       cm/get-links
       (filter #(= "alternate" (:rel (:attrs %))))
       (filter #(= "application/atom+xml" (:type (:attrs %))))
       (map #(-> % :attrs :href))
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

