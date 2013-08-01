(ns jiksnu.util
  (:use [ciste.config :only [config environment]]
        [ciste.loader :only [require-namespaces]]
        [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [*base-url*]]
        [clojure.core.incubator :only [-?> -?>>]]
        [slingshot.slingshot :only [throw+ try+]]
        [lamina.executor :only [task]])
  (:require [ciste.model :as cm]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [lamina.core :as l]
            [lamina.time :as time]
            [lamina.trace :as trace]
            monger.joda-time
            monger.json
            [org.bovinegenius.exploding-fish :as uri]
            [ring.util.codec :as codec])
  (:import com.mongodb.WriteConcern
           com.ocpsoft.pretty.time.PrettyTime
           java.io.FileNotFoundException
           java.io.PrintWriter
           java.text.SimpleDateFormat
           java.util.Date
           java.net.InetAddress
           java.net.InetSocketAddress
           java.net.Socket
           java.net.URI
           java.net.URL
           lamina.core.channel.Channel
           org.apache.axiom.util.UIDGenerator
           org.bson.types.ObjectId
           org.joda.time.DateTime
           org.jsoup.Jsoup
           org.jsoup.safety.Whitelist
           java.io.StringReader))

(defn new-id
  []
  (let [id (UIDGenerator/generateURNString)]
    (trace/trace :id:generated id)
    id))

(defn format-date
  "This is a dirty little function to get a properly formatted date."
  ;; TODO: Get more control of passed dates
  [date]
  ;; TODO: Time for a protocol
  (condp = (class date)
    String (DateTime/parse date)
    DateTime date
    Date (let [formatter (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'")]
           (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
           (.format formatter  date))
    date))

(defn strip-namespaces
  [val]
  (-?> val
       (string/replace #"http://activitystrea.ms/schema/1.0/" "")
       (string/replace #"http://ostatus.org/schema/1.0/" "")))

(defn path-segments
  [url]
  (if url
    (let [url-obj (URL. url)
          path (.getPath url-obj)
          ps (string/split path #"/")
          ;; TODO: get scheme
          bare (str "http://" (.getHost url-obj))]
      (map #(str bare % "/")
           (reductions (fn [s1 s2] (string/join "/" [s1 s2]))
                       (drop-last ps))))
    []))

(defn rel-filter
  "returns all the links in the collection where the rel value matches the
   supplied value"
  ([rel links] (rel-filter rel links nil))
  ([rel links content-type]
     (filter (fn [link]
               (and (= (:rel link) rel)
                    (or (not content-type) (= (:type link) content-type))))
             links)))

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

(defn force-coll
  [x]
  (if (coll? x)
    x (list x)))

(defn ^ObjectId make-id
  "Create an object id from the provided string"
  ([] (ObjectId.))
  ([^String id] (ObjectId. id)))

;; link functions

(defn find-atom-link
  [links]
  (->> links
       (filter #(= "alternate" (:rel (:attrs %))))
       (filter #(= "application/atom+xml" (:type (:attrs %))))
       (map #(-> % :attrs :href))
       first))

(defn extract-atom-link
  "Find the atom link in the page identified by url"
  [url]
  (->> url
       cm/get-links
       find-atom-link))

(defn parse-http-link
  [url]
  (let [[_ href remainder] (re-matches #"<([^>]+)>; (.*)" url)]
    (->> (string/split remainder #"; ")
         (map #(let [[_ k v] (re-matches #"(.*)=\"(.*)\"" %)] [k v]))
         (into {})
         (merge {"href" href}))))

;; hooks

(defn add-hook!
  [r f]
  (dosync
   (alter r conj f)))

;; serializers

(defn date->twitter
  [date]
  (let [formatter (SimpleDateFormat. "EEE MMM d HH:mm:ss Z yyyy")]
    (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
    (.format formatter date)))

(defn prettyify-time
  [^Date date]
  (-?>> date (.format (PrettyTime.))))

(defn date->rfc1123
  [date]
  (let [formatter (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss 'GMT'")]
    (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
    (.format formatter date)))

(defn write-json-date
  ([^Date date ^PrintWriter out]
     (write-json-date date out false))
  ([^Date date ^PrintWriter out escape-unicode?]
     (let [formatted-date (.format (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") date)]
       (.print out (str "\"" formatted-date "\"")))))

(defn write-json-object-id
  ([id ^PrintWriter out]
     (write-json-object-id id out false))
  ([id ^PrintWriter out escape-unicode]
     (.print out (str "\"" id "\""))))

(extend Date json/JSONWriter {:-write write-json-date})
(extend ObjectId json/JSONWriter {:-write write-json-object-id})

(defn split-uri
  "accepts a uri in the form of username@domain or scheme:username@domain and
   returns a vector containing both the username and the domain"
  [uri]
  (let [[_ _ username domain] (re-find #"(.*:)?([^@]+)@([^\?]+)" uri)]
    (when (and username domain) [username domain])))

(defn get-domain-name
  "Takes a string representing a uri and returns the domain"
  [id]
  (let [{:keys [path scheme] :as uri} (uri/uri id)]
    (condp = scheme
      "acct" (second (split-uri id))
      "urn"  (let [parts (string/split path #":")
                   nid (nth parts 0)]
               (condp = nid
                 "X-dfrn" (nth parts 1)))
      (:host uri))))

(defn parse-link
  [link]
  (let [rel (.getAttributeValue link "rel")
        template (.getAttributeValue link "template")
        href (.getAttributeValue link "href")
        type (.getAttributeValue link "type")
        lang (.getAttributeValue link "lang")
        title (if-let [title-element (.getFirstChildElement link "Title" ns/xrd)]
                (.getValue title-element))]
    (merge (when rel      {:rel rel})
           (when template {:template template})
           (when href     {:href href})
           (when type     {:type type})
           (when title {:title title})
           (when lang     {:lang lang}))))

(defn sanitize
  [input]
  (Jsoup/clean input (Whitelist/none)))

;; (defmacro safe-task
;;   [& body]
;;   `(let [b# (do ~@body)
;;          res# (l/result-channel)]
;;      (l/enqueue res# b#)
;;      (l/on-realized res#
;;                     identity
;;                     #(trace/trace :errors:handled %))
;;      res#))

(defmacro safe-task
  [& body]
  `(let [res#
         ;; (trace/time*
          (task ~@body)
          ;; )
         ]
     (l/on-realized res#
                    identity
                    #(trace/trace :errors:handled %))
     res#))

(defn require-module
  [prefix module-name model-name]
  (require-namespaces
   (map
    (fn [part-name]
      (format "%s.%s.%s.%s-%s"
              prefix
              module-name
              part-name
              model-name
              part-name))
    ["filters"
     "sections"
     "triggers"
     "views"])))

(defn replace-template
  [template url]
  (string/replace template #"\{uri\}" (codec/url-encode url)))

(defn socket-conectable?
  [host port]
  (let [socket (Socket.)
        address (InetAddress/getByName host)
        socket-address (InetSocketAddress. address port)]
    (try+
     (.connect socket socket-address)
     true
     (catch Object ex false)
     (finally
       (.close socket)))))
