(ns jiksnu.atom.view
  (:use jiksnu.model
        clojure.contrib.logging
        ciste.view
        jiksnu.view)
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

