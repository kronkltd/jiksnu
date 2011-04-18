(ns jiksnu.abdera
  (:use jiksnu.model
        [clojure.contrib.logging :only (error)])
  (:import java.io.ByteArrayInputStream
           javax.xml.namespace.QName))

(defonce #^:dynamic *abdera-client* (AbderaClient.))

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
  ([element]
     (com.cliqset.abdera.ext.activity.Object. element))
  ([namespace name prefix]
     (com.cliqset.abdera.ext.activity.Object.
      *abdera-factory* (QName. namespace name prefix))))

;; (defn node-value
;;   [^Element element]
;;   (.getAttribute element "node"))

(defn find-children
  [element path]
  (if element
    (.findChild element path)))

;; (defn ns-prefix
;;   [k]
;;   (apply str
;;          "xmlns"
;;          (if (not= k "")
;;            (list ":" k))))

;; (defn element?
;;   "Returns if the argument is an element"
;;   [arg]
;;   (instance? Element arg))

