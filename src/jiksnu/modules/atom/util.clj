(ns jiksnu.modules.atom.util
  (:use [ciste.initializer :only [definitializer]]
        [clojure.core.incubator :only [-?>]]
        [slingshot.slingshot :only [try+]])
  (:require [clj-statsd :as s]
            [clj-tigase.element :as element]
            [clj-time.coerce :as coerce]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import java.io.ByteArrayInputStream
           java.net.URI
           javax.xml.namespace.QName
           org.apache.abdera.Abdera
           ;; org.apache.abdera.ext.thread.ThreadHelper
           org.apache.abdera.factory.Factory
           org.apache.abdera.model.Element
           org.apache.abdera.model.Entry
           org.apache.abdera.model.ExtensibleElement
           org.apache.abdera.model.Feed
           org.apache.abdera.model.Link
           org.apache.abdera.model.Person
           org.apache.abdera.protocol.client.AbderaClient
           ))

(declare make-link)
(declare abdera)
(declare abdera-factory)
(declare abdera-client)
(declare abdera-parser)

(defn ^Entry new-entry
  []
  (.newEntry abdera))

(defn get-text
  [^Element element]
  (.getText element))

(defn find-children
  [^Element element path]
  (.findChild element path))

(defn get-qname
  "Returns a map representing the QName of the given element"
  [^Element element]
  (element/parse-qname (.getQName element)))

(defn rule-element?
  [^Element element]
  (= (.getName element) "acl-rule"))

(defn get-extension
  [^ExtensibleElement element
   ^String ns-part
   ^String local-part]
  (.getExtension element (QName. ns-part local-part)))


(defn get-entries
  [^Feed feed]
  (-> feed .getEntries seq))

(defn rel-filter-feed
  [^Feed feed rel]
  (if feed
    (filter
     (fn [link] (= (.getRel link) rel))
     (.getLinks feed))))

(defn get-hub-link
  [^Feed feed]
  (-?> feed
       (rel-filter-feed "hub")
       first
       .getHref
       str))

(defn add-link
  [^Feed feed link]
  (.addLink feed (make-link link)))

(defn get-feed-updated
  [^Feed feed]
  (.getUpdated feed))

(defn get-href
  "get the href from a link as a string"
  [^Link link]
  (str (.getHref link)))

(defn parse-link
  "Returns a map representing the link element"
  [^Link link]
  (let [type (try+ (str (.getMimeType link))
                   (catch Object  ex
                     #_(trace/trace "errors:handled" ex)))
        extensions (map
                    #(.getAttributeValue link  %)
                    (.getExtensionAttributes link))
        title (.getTitle link)
        href (str (.getHref link))
        rel (.getRel link)]
    (merge (when (seq href)       {:href href})
           (when (seq rel)        {:rel rel})
           (when (seq title)      {:title title})
           (when (seq extensions) {:extensions extensions})
           (when (seq type)       {:type (str type)}))))

(defn attr-val
  [^Element element name]
  (.getAttributeValue element name))

(defn parse-notice-info
  "extract the notice info from a statusnet element"
  [^Element element]
  (let [source (util/sanitize (attr-val element "source"))
        local-id (attr-val element "local_id")
        source-link (attr-val element "source_link")]
    {:source {:name source
              :href source-link}
     :jiksnu {:local-id local-id}}))

(defn parse-irts
  "Get the in-reply-to uris"
  [^Entry entry]
  (->> [] #_(ThreadHelper/getInReplyTos entry)
       (map #(str (.getHref %)))
       (filter identity)))

(defn get-comment-count
  [^Entry entry]
  (or
   (if-let [link (.getLink entry "replies")]
     (let [count-qname (QName.
                        "http://purl.org/syndication/thread/1.0"
                        "count" )]
       (if-let [count-attr (attr-val link count-qname)]
         (Integer/parseInt count-attr))))
   0))

(defn parse-tags
  [^Entry entry]
  (let [categories (.getCategories entry)]
    (map
     (fn [category] (.getTerm category))
     categories)))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))

(defn ^URI author-uri
  "Returns the uri of the author"
  [^Entry entry]
  (let [author (.getAuthor entry)]
    ;; TODO: This is wasteful, why am I doing this?
    (let [uri (.getUri author)]
      (URI. (str uri)))))

(defn parse-links
  [^Entry entry]
  (map parse-link (.getLinks entry)))



(defn get-feed-author
  [^Feed feed]
  (if feed (first (.getAuthors feed))))



(defn ^Person get-author
  [^Entry entry feed]
  (or (.getAuthor entry)
      (get-feed-author feed)
      (-?> entry .getSource .getAuthors first)))

(defn ^Person make-person
  "Takes a map of params and returns an initialized person object"
  [{:keys [name extensions] :as params}]
  (let [person (.newAuthor abdera-factory)]
    (when name
      (.setName person name))
    (doseq [extension extensions]
      (.addExtension (:ns extension) (:local extension) (:prefix extension) (:element extension)))
    person))


(defn get-name
  "Returns the name of the Atom person"
  [^Person person]
  (or (.getSimpleExtension person ns/poco "displayName" "poco" )
      (.getName person)))

(defn get-extension-elements
  [^Person person ns-part local-part]
  (.getExtensions person (QName. ns-part local-part)))

(defn get-links
  [^Person person]
  (-> person
      (get-extension-elements ns/atom "link")
      (->> (map parse-link))))

(defn get-simple-extension
  [^Person person ns-part local-part]
  (.getSimpleExtension person (QName. ns-part local-part)))

(defn get-username
  [^Person person]
  (get-simple-extension person ns/poco "preferredUsername"))

(defn get-note
  [^Person person]
  (get-simple-extension person ns/poco "note"))


(defn ^Link make-link
  "Convert a map to an abdera link"
  [link-map]
  (let [{:keys [href rel type attributes]} link-map
        link (.newLink abdera-factory)]
    (when href (.setHref link href))
    (when rel (.setRel link rel))
    (when type (.setMimeType link type))
    (when attributes
      (doseq [{:keys [name value]} attributes]
        (.setAttributeValue link name value)))
    link))

(defn ^Feed make-feed*
  "Convert a feed map into a feed"
  [{:keys [author title subtitle links entries updated id generator] :as feed-map}]
  (let [feed (.newFeed abdera)]
    (when title (.setTitle feed title))
    (when subtitle (.setSubtitle feed subtitle))
    (when generator
      (let [{:keys [uri name version]} generator]
        (.setGenerator feed uri name version)))
    (when id (.setId feed id))
    (when updated (.setUpdated feed (coerce/to-date updated)))
    (when author (.addExtension feed author))
    (doseq [link links]
      (add-link feed link))
    (doseq [entry entries]
      (.addEntry feed entry))
    feed))

;; TODO: should return the actual map
(defn make-feed
  "Returns the string representation of a feed from a feed map"
  [m]
  (str (make-feed* m)))

(defn parse-stream
  [stream]
  (try
    (let [parser abdera-parser
          feed (.parse parser stream)]
      (trace/trace :feed:parsed feed)
      feed)
    (catch IllegalStateException ex
      (trace/trace "errors:handled" ex))))

(defn stream->feed
  [stream]
  (.getRoot (parse-stream stream)))

(defn parse-xml-string
  "Converts a string to an Abdera entry"
  [^String entry-string]
  (let [stream (ByteArrayInputStream. (.getBytes entry-string "UTF-8"))
        parsed (parse-stream stream)]
    (.getRoot parsed)))

(definitializer
  (defonce ^Abdera abdera (Abdera/getInstance))
  (defonce ^Factory abdera-factory (.getFactory abdera))
  (defonce abdera-parser (.getParser abdera))
  (defonce abdera-client (AbderaClient.)))
