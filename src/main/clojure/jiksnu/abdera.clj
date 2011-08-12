(ns jiksnu.abdera
  (:use ciste.debug
        [clojure.tools.logging :only (error)])
  (:require [clj-tigase.element :as element]
            [jiksnu.model.user :as model.user])
  (:import com.cliqset.abdera.ext.activity.ActivityExtensionFactory
           com.cliqset.abdera.ext.poco.PocoExtensionFactory
           java.io.ByteArrayInputStream
           java.io.StringWriter
           javax.xml.namespace.QName
           org.apache.abdera.Abdera
           org.apache.abdera.ext.json.JSONUtil
           org.apache.abdera.factory.Factory
           org.apache.abdera.model.Element
           org.apache.abdera.model.Entry
           org.apache.abdera.protocol.client.AbderaClient))

(defonce ^Abdera ^:dynamic *abdera* (Abdera.))
(defonce ^Factory ^:dynamic *abdera-factory* (.getFactory *abdera*))
(defonce ^:dynamic *abdera-parser* (.getParser *abdera*))
(defonce ^:dynamic *abdera-client* (AbderaClient.))

(.registerExtension *abdera-factory* (ActivityExtensionFactory.))
(.registerExtension *abdera-factory* (PocoExtensionFactory.))

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

(defn get-author-id
  [author]
  (let [uri (.getUri author)
        domain (.getHost uri)
        name (or (.getUserInfo uri)
                 (.getName author))
        author-obj (model.user/find-or-create name domain)]
    (:_id author-obj)))

(defn to-json
  "Serializes an Abdera entry to a json StringWriter"
  [^Entry entry]
  (let [string-writer (StringWriter.)]
    (JSONUtil/toJson entry string-writer)
    string-writer))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))

