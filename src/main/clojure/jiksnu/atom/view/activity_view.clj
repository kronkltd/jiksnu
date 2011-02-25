(ns jiksnu.atom.view.activity-view
  (:use jiksnu.model
        jiksnu.atom.view
        jiksnu.http.view
        jiksnu.view
        clojure.contrib.logging
        ciste.view
        [karras.entity :only (make)]
        jiksnu.namespace)
  (:require [jiksnu.model.user :as model.user]
            [jiksnu.atom.view.user-view :as view.user])
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Element
           org.apache.abdera.ext.json.JSONUtil
           com.cliqset.abdera.ext.activity.object.Person
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
  [^Entry entry ^Activity activity]
  (doseq [extension (:extensions activity)]
    (.addExtension entry (parse-json-element extension))))

(defn make-object
  [^Element element]
  (com.cliqset.abdera.ext.activity.Object. element))

(defn add-author
  [^Entry entry author-id]
  (if-let [user (model.user/fetch-by-id author-id)]
    (let [author-element (.addExtension entry atom-ns "author" "")
          author (Person. (make-object author-element))
          author-uri (full-uri user)
          author-name (:name user)
          actor-element (.addExtension entry as-ns "actor" "activity")]
      (doto author
        (.setObjectType person-uri)
        (.setId (str "acct:" (:username user)
                     "@" (:domain user)))
        (.setName (:first-name user) (:last-name user))
        (.setDisplayName (:name user))
        (.addSimpleExtension atom-ns "email" ""
                             (or (:email user) (str (:username user)
                                                    "@" (:domain user))))
        (.addSimpleExtension atom-ns "name" "" (:name user))
        (.addAvatar (:avatar-url user) "image/jpeg")
        (.addSimpleExtension atom-ns "uri" "" author-uri))
      (.addExtension entry author)
      (doto actor-element
        (.addSimpleExtension (QName. atom-ns "name") author-name)
        (.addSimpleExtension (QName. atom-ns "email") author-uri)
        (.addSimpleExtension (QName. atom-ns "uri") author-uri)))))

(defn add-authors
  [^Entry entry ^Activity activity]
  (dorun
   (map (partial add-author entry)
        (:authors activity)))
  entry)

(defn to-json
  "Serializes an Abdera entry to a json StringWriter"
  [^Entry entry]
  (let [string-writer (StringWriter.)]
    (JSONUtil/toJson entry string-writer)
    string-writer))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  [^Entry entry]
  ;; (println "entry: " entry)
  (let [id (.getId entry)
        title (.getTitle entry)
        published (.getPublished entry)]
    (make Activity {:_id id
                    :title title
                    :published published})))

(defn ^Entry to-entry
  "Takes a json object that matches the results of serializing an Abdera
entry and converts it to an entry"
  [^Activity activity]
  (let [entry (new-entry)]
    (doto entry
      (.setId (:_id activity))
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setTitle (or (:title activity) (:summary activity)))
      (add-authors activity)
      ;; TODO: add acl rules
      ;; TODO: add verb
      (.addLink (full-uri activity) "alternate")
      (.setContentAsHtml (:summary activity))
      (.addSimpleExtension as-ns "object-type" "activity" status-uri)
      (.addSimpleExtension as-ns "verb" "activity" post-uri)
      (add-extensions activity))
    (let [object-element (.addExtension entry as-ns "object" "activity")]
      (.setObjectType object-element status-uri)
      (.setContentAsHtml object-element (:summary activity)))
    entry))
