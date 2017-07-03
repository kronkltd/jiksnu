(ns jiksnu.util
  (:require [ciste.loader :refer [require-namespaces]]
            [clj-factory.core :refer [factory]]
            [clj-time.format :as f]
            [clojure.string :as string]
            [crypto.random :as random]
            [jiksnu.namespace :as ns]
            [jiksnu.registry :as registry]
            [manifold.deferred :as d]
            monger.joda-time
            monger.json
            [org.bovinegenius.exploding-fish :as uri]
            [puget.printer :as puget]
            [ring.util.codec :as codec]
            [slingshot.slingshot :refer [try+]]
            [taoensso.timbre :as timbre])
  (:import java.net.InetAddress
           java.net.Socket
           java.net.URL
           java.text.SimpleDateFormat
           java.util.Date
           java.util.UUID
           (java.util TimeZone)
           (java.net InetSocketAddress)
           (org.bson.types ObjectId)
           (org.joda.time DateTime)))

(defn new-id
  []
  (let [id (str (UUID/randomUUID))]
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
           (.setTimeZone formatter (TimeZone/getTimeZone "UTC"))
           (.format formatter date))
    date))

(defn path-segments
  [url]
  (if url
    (let [url-obj (URL. url)
          path (.getPath url-obj)
          ps (string/split path #"/")]
      (map #(str % "/")
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

(defn params-encode
  [params]
  (->> params
       (map (fn [[k v]] (str (name k) "=" v)))
       (string/join "&")))

;; TODO: I'm sure this exists somewhere else
;; all this is really doing is assembling a uri
(defn make-subscribe-uri
  [url options]
  (str url "?" (params-encode options)))

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
    (.setTimeZone formatter (TimeZone/getTimeZone "UTC"))
    (.format formatter date)))

(defn date->rfc1123
  [date]
  (let [formatter (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss 'GMT'")]
    (.setTimeZone formatter (TimeZone/getTimeZone "UTC"))
    (.format formatter date)))

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
      "urn" (let [parts (string/split path #":")
                  nid (nth parts 0)]
              (condp = nid
                "X-dfrn" (nth parts 1)))
      (:host uri))))

(defmacro safe-task
  [& body]
  `(let [res# ~@body]
     (d/on-realized (d/future res#)
                    identity identity)
     res#))

(defn replace-template
  [template url]
  (string/replace template #"\{uri\}" (codec/url-encode url)))

(defn socket-conectable?
  [^String host ^long port]
  (let [socket (Socket.)]
    (try+
     (let [address (InetAddress/getByName host)
           socket-address (InetSocketAddress. address port)]
       (.connect socket socket-address))
     true
     (catch Object ex false)
     (finally
       (.close socket)))))

(defn generate-token
  ([] (generate-token 16))
  ([length]
   (-> (random/base32 length)
       (string/replace #"\+" "-")
       (string/replace #"/" "_")
       (string/replace #"=" ""))))

(def time-handlers
  {ObjectId
   (puget/tagged-handler
    'ObjectId
    (partial str))
   DateTime
   (puget/tagged-handler
    'inst
    (partial f/unparse (f/formatters :date-time)))})

(defmacro inspect
  "Prints a display of the passed value"
  [v]
  `(let [val# ~v]
     (timbre/infof "%s => %s"
                   (puget/cprint-str (quote ~v))
                   (puget/cprint-str val# {:print-handlers time-handlers}))
     val#))
