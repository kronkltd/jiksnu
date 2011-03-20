(ns jiksnu.atom.view.activity-view
  (:use ciste.sections
        ciste.view
        clojure.contrib.logging
        jiksnu.atom.view
        jiksnu.http.view
        jiksnu.model
        jiksnu.namespace
        jiksnu.view
        [karras.entity :only (make)])
  (:require [jiksnu.model.user :as model.user]
            [jiksnu.atom.view.user-view :as view.user])
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.ext.json.JSONUtil
           org.apache.abdera.model.Element
           org.apache.abdera.model.Entry))

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
    (let [author-name (:name user)
          author-jid  (str (:username user) "@" (:domain user))
          actor-element (.addExtension entry as-ns "actor" "activity")]
      (doto actor-element
        (.addSimpleExtension atom-ns "name" "" author-name)
        (.addSimpleExtension atom-ns "email" "" author-jid)
        (.addSimpleExtension atom-ns "uri" "" author-jid))
      (.addExtension entry actor-element)
      (.addExtension entry (show-section user)))))

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

(defn parse-extension-element
  [element]
  (let [qname (.getQName element)
        name (.getLocalPart qname)
        namespace (.getNamespaceURI qname)]
    (if (and (= name "actor")
             (= namespace as-ns))
      (let [uri (.getSimpleExtension element atom-ns "uri" "")]
        {:authors [(:_id (model.user/find-or-create-by-uri uri))]})
      (if (and (= name "object")
                 (= namespace as-ns))
        (let [object (make-object element)]
          {:type (str (.getObjectType object))
           :object-id (str (.getId object))
           :object-updated (.getUpdated object)
           :object-published (.getPublished object)
           :object-content (.getContent object)})))))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  [^Entry entry]
  (let [id (str (.getId entry))
        title (.getTitle entry)
        published (.getPublished entry)
        updated (.getUpdated entry)
        authors (.getAuthors entry)]
    (doall
     (map
      (fn [author]
        (println "author: " author))
      authors))
    (let [extension-maps
          (doall
           (map
            parse-extension-element
            (.getExtensions entry)))]
      (make Activity (apply merge
                            (if published {:published published})
                            (if updated {:updated updated})
                            (if title {:title title})
                            {:_id id}
                            extension-maps)))))

(defn comment-link
  [entry activity]
  (if (:comments activity)
    (let [comment-count (count (:comments activity))]
      (let [thread-link (.newLink *abdera-factory*)]
        (.setRel thread-link "replies")
        (.setAttributeValue thread-link "count" (str comment-count))
        (.setMimeType thread-link "application/atom+xml")
        (.addLink entry thread-link)))))

(defn acl-link
  [entry activity]
  (if (:public activity)
    (let [rule-element (.addExtension entry osw-uri "acl-rule" "")]
      (let [action-element
            (.addSimpleExtension rule-element osw-uri
                                 "acl-action" "" view-uri)]
        (.setAttributeValue action-element "permission" grant-uri))
      (let [subject-element
            (.addExtension rule-element osw-uri "acl-subject" "")]
        (.setAttributeValue subject-element "type" everyone-uri)))))

(defsection show-section [Activity :atom]
  [^Activity activity & _]
  (let [entry (new-entry)]
    (doto entry
      (.setId (:_id activity))
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setTitle (or (and (not= (:title activity) "")
                          (:title activity))
                     (:summary activity)))
      (add-authors activity)
      (.addLink (full-uri activity) "alternate")
      (.setContentAsHtml (:summary activity))
      (.addSimpleExtension as-ns "object-type" "activity" status-uri)
      (.addSimpleExtension as-ns "verb" "activity" post-uri)
      (add-extensions activity)
      (comment-link activity)
      (acl-link activity))
    (let [object-element (.addExtension entry as-ns "object" "activity")]
      (.setObjectType object-element status-uri)
      (if-let [object-updated (:object-updated activity)]
        (.setUpdated object-element object-updated))
      (if-let [object-published (:object-published activity)]
        (.setPublished object-element object-published))
      (if-let [object-id (:object-id activity)]
        (.setId object-element object-id))
      (.setContentAsHtml object-element (:summary activity)))
    entry))
