(ns jiksnu.abdera
  (:use jiksnu.model
        [clojure.contrib.logging :only (error)])
  (:import java.io.ByteArrayInputStream))

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

