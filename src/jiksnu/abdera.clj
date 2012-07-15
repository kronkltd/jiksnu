(ns jiksnu.abdera
  (:use [clojure.core.incubator :only [-?>]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log])
  (:import java.io.ByteArrayInputStream
           java.io.StringWriter
           java.net.URI
           javax.xml.namespace.QName
           org.apache.abdera2.Abdera
           org.apache.abdera2.ext.thread.ThreadHelper
           org.apache.abdera2.factory.Factory
           org.apache.abdera2.model.Element
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.Feed
           org.apache.abdera2.model.Link
           org.apache.abdera2.protocol.client.AbderaClient
           org.apache.axiom.util.UIDGenerator))

(defonce ^Abdera ^:dynamic *abdera* (Abdera/getInstance))
(defonce ^Factory ^:dynamic *abdera-factory* (.getFactory *abdera*))
(defonce ^:dynamic *abdera-parser* (.getParser *abdera*))
(defonce ^:dynamic *abdera-client* (AbderaClient.))

;; TODO: Since I am no longer using this style of id, I am not sure if
;; this is still needed. Perhaps move to abdera
(defn new-id
  []
  (UIDGenerator/generateURNString))

(defn get-text
  [^Element element]
  (.getText element))

(defn ^Entry new-entry
  []
  (.newEntry *abdera*))

(defn fetch-resource
  [uri]
  (.get (AbderaClient.) uri))

(defn fetch-document
  [uri]
  (.getDocument (fetch-resource uri)))

(defn fetch-feed
  [uri]
  (.getRoot (fetch-resource uri)))

(defn get-entries
  [^Feed feed]
  (-> feed .getEntries seq))

(defn get-href
  "get the href from a link as a string"
  [^Link link]
  (str (.getHref link)))

(defn parse-irts
  "Get the in-reply-to uris"
  [entry]
  (->> (ThreadHelper/getInReplyTos entry)
       (map #(str (.getHref %)))
       (filter identity)))

(defn parse-link
  "extract the node element from links

this is for OSW
"
  [link]
  (if-let [href (get-href link)]
    (when (and (re-find #"^.+@.+$" href)
               (not (re-find #"node=" href)))
      href)))



(defn parse-stream
  [stream]
  (try
    (let [parser *abdera-parser*]
      (.parse parser stream))
    (catch IllegalStateException e
      (log/error e))))

(defn parse-xml-string
  "Converts a string to an Abdera entry"
  [^String entry-string]
  (let [stream (ByteArrayInputStream. (.getBytes entry-string "UTF-8"))
        parsed (parse-stream stream)]
    (.getRoot parsed)))

(defn not-namespace
  "Filter for map entries that do not represent namespaces"
  [[k v]]
  (not (= k :xmlns)))

(defn find-children
  [element path]
  (.findChild element path))

(defn get-qname
  "Returns a map representing the QName of the given element"
  [^Element element]
  (element/parse-qname (.getQName element)))

(defn get-comment-count
  [^Entry entry]
  (or
   (if-let [link (.getLink entry "replies")]
     (let [count-qname (QName.
                        "http://purl.org/syndication/thread/1.0"
                        "count" )]
       (if-let [count-attr (.getAttributeValue link count-qname)]
         (Integer/parseInt count-attr))))
   0))

(defn parse-tags
  [^Entry entry]
  (let [categories (.getCategories entry)]
    (map
     (fn [category] (.getTerm category))
     categories)))

;; (defn get-author-id
;;   [author]
;;   (let [uri (.getUri author)
;;         domain (.getHost uri)
;;         name (or (.getUserInfo uri)
;;                  (.getName author))]
;;     (str name "@" domain)))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))


(defn rel-filter-feed
  [^Feed feed rel]
  (if feed
    (filter
     (fn [link] (= (.getRel link) rel))
     (.getLinks feed))))

(defn ^URI author-uri
  "Returns the uri of the author"
  [^Entry entry]
  (let [author (.getAuthor entry)]
    ;; TODO: This is wasteful, why am I doing this?
    (let [uri (.getUri author)]
      (URI. (.toString uri)))))

(defn rule-element?
  [^Element element]
  (= (.getName element) "acl-rule"))

(defn get-hub-link
  [feed]
  (-?> feed
       (rel-filter-feed "hub")
       first
       .getHref
       str))

(defn get-author
  [entry feed]
  (or
   ;; (.getActor entry)
   (.getAuthor entry)
   (if feed (first (.getAuthors feed)))
   (if-let [source (.getSource entry)]
     (first (.getAuthors source)))))

(defn ^Link make-link
  "Convert a map to an abdera link"
  [link]
  (let [{:keys [href rel type attributes]} link
        link-element (.newLink *abdera-factory*)]
    (when href (.setHref link-element href))
    (when rel (.setRel link-element rel))
    (when type (.setMimeType link-element type))
    (when attributes
      (doseq [{:keys [name value]} attributes]
        (.setAttributeValue link-element name value)))
    link-element))

(defn add-link
  [feed link]
  (.addLink feed (make-link link)))

(defn make-feed*
  [{:keys [author title subtitle links entries updated id generator]}]
  (let [feed (.newFeed *abdera*)]
    (when title (.setTitle feed title))
    (when subtitle (.setSubtitle feed subtitle))
    (when generator
      (let [{:keys [uri name version]} generator]
        (.setGenerator feed uri name version)))
    (when id (.setId feed id))
    (when updated (.setUpdated feed updated))
    (when author (.addExtension feed author))
    (doseq [link links]
      (add-link feed link))
    (doseq [entry entries]
      (.addEntry feed entry)
      #_(add-entry feed entry))
    feed))

;; TODO: should return the actual map
(defn make-feed
  "Returns the string representation of a feed from a feed map"
  [m]
  (str (make-feed* m)))

(defn parse-link
  [^Link link]
  (let [type (try (str (.getMimeType link)) (catch Exception ex))
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

(defn parse-links
  [entry]
  (map parse-link (.getLinks entry)))

;; (defn parse-object-element
;;   [element]
;;   #_(let [object (make-object element)]
;;       {:object {:object-type (str (.getObjectType object))
;;                 :links (parse-links object)}
;;        :id (str (.getId object))
;;        :updated (.getUpdated object)
;;        :published (.getPublished object)
;;        :content (.getContent object)}))

;; Deprecated
(defn parse-json-element
  "Takes a json object representing an Abdera element and converts it to
an Element"
  ([activity]
     (parse-json-element activity ""))
  ([{children :children
     attributes :attributes
     element-name :name
     :as activity} bound-ns]
     (let [xmlns (or (:xmlns attributes) bound-ns)
           qname (QName. xmlns element-name)
           element (.newExtensionElement *abdera-factory* qname)
           filtered (filter not-namespace attributes)]
       (doseq [[k v] filtered]
         (.setAttributeValue element (name k) v))
       (doseq [child children]
         (if (map? child)
           (.addExtension element (parse-json-element child xmlns))
           (when (string? child)
             (.setText element child))))
       element)))

(defn stream->feed
  [stream]
  (.getRoot (parse-stream stream)))
