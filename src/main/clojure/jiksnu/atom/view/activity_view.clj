(ns jiksnu.atom.view.activity-view
  (:use jiksnu.model
        jiksnu.atom.view
        jiksnu.http.view
        jiksnu.view
        clojure.contrib.logging
        ciste.view
        [karras.entity :only (make)]
        [jiksnu.namespace :only (as-ns atom-ns)])
  (:require [jiksnu.model.user :as model.user]
            [jiksnu.atom.view.user-view :as view.user])
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Element
           org.apache.abdera.ext.json.JSONUtil
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.User))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))

(defn parse-json-element
  "Takes a json object representing an Abdera element and converts it to
an Element"
  ([activity]
     (parse-json-element activity ""))
  ([{children :children
     attributes :attributes
     element-name :name
     :as activity} bound-ns]
     (let [xmlns (or (:xmlns attributes) bound-ns)]
       (let [qname (QName. xmlns element-name)
             element (.newExtensionElement *abdera-factory* qname)
             filtered (filter not-namespace attributes)]
         (doseq [[k v] filtered]
           (.setAttributeValue element (name k) v))
         (doseq [child children]
           (if (map? child)
             (.addExtension element (parse-json-element child xmlns))
             (if (string? child)
               (.setText element child))))
         element))))

(defn ^Entry new-entry
  [& opts]
  (let [entry (.newEntry *abdera*)]
    entry))

(defn add-extensions
  [^Entry entry
   ^Activity activity]
  (doseq [extension (:extensions activity)]
    (.addExtension entry (parse-json-element extension))))

(defn add-author
  [^Entry entry author-id]
  (if-let [user (model.user/show author-id)]
    (let [author (.newAuthor *abdera-factory*)
          author-uri (full-uri user)
          author-name (:name user)
          actor-element (.addExtension entry (QName. as-ns "actor"))]
      (doto author
        (.setName author-name)
        (.setEmail (view.user/get-uri user))
        (.setUri author-uri))
      (.addAuthor entry author)
      (doto actor-element
        (.addSimpleExtension (QName. atom-ns "name") author-name)
        (.addSimpleExtension (QName. atom-ns "email") author-uri)
        (.addSimpleExtension (QName. atom-ns "uri") author-uri)))))

(defn add-authors
  [^Entry entry
   ^Activity activity]
  (dorun
   (map (partial add-author entry)
        (:authors activity)))
  entry)

(defn to-json
  "Serializes an Abdera entry to a json StringWriter"
  [^Entry entry]
  (debug (str "entry: " entry))
  (let [string-writer (StringWriter.)]
    (JSONUtil/toJson entry string-writer)
    string-writer))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  [^Entry entry]
  (let [json-map {}
        id (.getId entry)]
    (make Activity {:_id id})))

(defn ^Entry to-entry
  "Takes a json object that matches the results of serializing an Abdera
entry and converts it to an entry"
  [^Activity activity]
  (let [entry (new-entry)]
    (doto entry
      (.setId (:_id activity))
      (.setTitle (:title activity))
      (.addLink (full-uri activity) "alternate")
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setContentAsHtml (:summary activity))
      (.addSimpleExtension
       (QName. as-ns
               "object-type"
               "activity") "post")
      (add-authors activity)
      (add-extensions activity))
    entry))

