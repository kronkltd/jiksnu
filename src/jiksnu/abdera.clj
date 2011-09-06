(ns jiksnu.abdera
  (:use (ciste [debug :only (spy)])
        (clojure.contrib [core :only (-?>)])
        [clojure.tools.logging :only (error)])
  (:require [clj-tigase.element :as element])
  (:import com.cliqset.abdera.ext.activity.ActivityExtensionFactory
           com.cliqset.abdera.ext.poco.PocoExtensionFactory
           java.io.ByteArrayInputStream
           java.io.StringWriter
           java.net.URI
           javax.xml.namespace.QName
           org.apache.abdera.Abdera
           org.apache.abdera.factory.Factory
           org.apache.abdera.model.Element
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Feed
           org.apache.abdera.protocol.client.AbderaClient
           org.apache.axiom.util.UIDGenerator))

(defonce ^Abdera ^:dynamic *abdera* (Abdera.))
(defonce ^Factory ^:dynamic *abdera-factory* (.getFactory *abdera*))
(defonce ^:dynamic *abdera-parser* (.getParser *abdera*))
(defonce ^:dynamic *abdera-client* (AbderaClient.))

(.registerExtension *abdera-factory* (ActivityExtensionFactory.))
(.registerExtension *abdera-factory* (PocoExtensionFactory.))

;; TODO: Since I am no longer using this style of id, I am not sure if
;; this is still needed. Perhaps move to abdera
(defn new-id
  []
  (UIDGenerator/generateURNString))

(defn get-text
  [element]
  (if element
    (.getCData element)))

(defn ^Entry new-entry
  [& opts]
  (.newEntry *abdera*))

(defn fetch-resource
  [uri]
  (.get (AbderaClient.) uri))

(defn fetch-document
  [uri]
  (.getDocument (fetch-resource uri)))

(defn fetch-feed
  [uri]
  (.getRoot (fetch-document uri)))

(defn fetch-entries
  [uri]
  (seq (.getEntries (fetch-feed uri))))

(defn parse-stream
  [stream]
  (try
    (let [parser *abdera-parser*]
      (.parse parser stream))
    (catch IllegalStateException e
      (error e))))

(defn parse-xml-string
  "Converts a string to an Abdera entry"
  [entry-string]
  (let [stream (ByteArrayInputStream. (.getBytes entry-string "UTF-8"))
        parsed (parse-stream stream)]
    (.getRoot parsed)))

(defn not-namespace
  "Filter for map entries that do not represent namespaces"
  [[k v]]
  (not (= k :xmlns)))

(defn make-object
  ([^Element element]
     (com.cliqset.abdera.ext.activity.Object. element))
  ([namespace name prefix]
     (com.cliqset.abdera.ext.activity.Object.
      *abdera-factory* (QName. namespace name prefix))))

(defn find-children
  [element path]
  (if element
    (.findChild element path)))

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
     (fn [link]
       (= (.getRel link) rel))
     (.getLinks feed))))

(defn author-uri
  [^Entry entry]
  (let [author (.getAuthor entry)]
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
