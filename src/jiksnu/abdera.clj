(ns jiksnu.abdera
  (:use [ciste.initializer :only [definitializer]]
        [clojure.core.incubator :only [-?>]])
  (:require [clj-statsd :as s]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns])
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
           org.apache.abdera2.model.Person
           org.apache.abdera2.protocol.client.AbderaClient
           org.apache.axiom.util.UIDGenerator))

(declare make-link)
(declare abdera)
(declare abdera-factory)
(declare abdera-client)
(declare abdera-parser)

;; TODO: Since I am no longer using this style of id, I am not sure if
;; this is still needed. Perhaps move to abdera
(defn new-id
  []
  (UIDGenerator/generateURNString))

(defn ^Entry new-entry
  []
  (.newEntry abdera))


(defn fetch-resource
  [uri]
  (s/increment "feed_fetched")
  (.get (AbderaClient.) uri))

(defn fetch-document
  [uri]
  (.getDocument (fetch-resource uri)))

(defn fetch-feed
  [uri]
  (log/debugf "Fetching feed: %s" uri)
  (.getRoot (fetch-resource uri)))

(defn not-namespace
  "Filter for map entries that do not represent namespaces"
  [[k v]]
  (not= k :xmlns))






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



(defn get-href
  "get the href from a link as a string"
  [^Link link]
  (str (.getHref link)))

(defn parse-link
  "extract the node element from links

this is for OSW
"
  [^Link link]
  (if-let [href (get-href link)]
    (when (and (re-find #"^.+@.+$" href)
               (not (re-find #"node=" href)))
      href)))

(defn parse-link
  "Returns a map representing the link element"
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



(defn parse-notice-info
  "extract the notice info from a statusnet element"
  [^Element element]
  (let [source (.getAttributeValue element "source")
        local-id (.getAttributeValue element "local_id")
        source-link (.getAttributeValue element "source_link")]
    {:source source
     :source-link source-link
     :local-id local-id}))

(defn attr-val
  [^Element element name]
  (.getAttributeValue element name))



(defn parse-irts
  "Get the in-reply-to uris"
  [^Entry entry]
  (->> (ThreadHelper/getInReplyTos entry)
       (map #(str (.getHref %)))
       (filter identity)))

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

(defn get-author
  [^Entry entry feed]
  (or
   ;; (.getActor entry)
   (.getAuthor entry)
   (if feed (first (.getAuthors feed)))
   (if-let [source (.getSource entry)]
     (first (.getAuthors source)))))




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

(defn get-extension
  [^Person person ns-part local-part]
  (.getSimpleExtension person (QName. ns-part local-part)))

(defn get-username
  [^Person person]
  (get-extension person ns/poco "preferredUsername"))

(defn get-note
  [^Person person]
  (get-extension person ns/poco "note"))


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
    (when updated (.setUpdated feed updated))
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
    (let [parser abdera-parser]
      (.parse parser stream))
    (catch IllegalStateException e
      (log/error e))))

(defn stream->feed
  [stream]
  (.getRoot (parse-stream stream)))




(defn parse-xml-string
  "Converts a string to an Abdera entry"
  [^String entry-string]
  (let [stream (ByteArrayInputStream. (.getBytes entry-string "UTF-8"))
        parsed (parse-stream stream)]
    (.getRoot parsed)))

;; (defn get-author-id
;;   [author]
;;   (let [uri (.getUri author)
;;         domain (.getHost uri)
;;         name (or (.getUserInfo uri)
;;                  (.getName author))]
;;     (str name "@" domain)))

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
;; (defn parse-json-element
;;   "Takes a json object representing an Abdera element and converts it to
;; an Element"
;;   ([activity]
;;      (parse-json-element activity ""))
;;   ([{children :children
;;      attributes :attributes
;;      element-name :name
;;      :as activity} bound-ns]
;;      (let [xmlns (or (:xmlns attributes) bound-ns)
;;            qname (QName. xmlns element-name)
;;            element (.newExtensionElement abdera-factory qname)
;;            filtered (filter not-namespace attributes)]
;;        (doseq [[k v] filtered]
;;          (.setAttributeValue element (name k) v))
;;        (doseq [child children]
;;          (if (map? child)
;;            (.addExtension element (parse-json-element child xmlns))
;;            (when (string? child)
;;              (.setText element child))))
;;        element)))

(definitializer
  (defonce ^Abdera abdera (Abdera/getInstance))
  (defonce ^Factory abdera-factory (.getFactory abdera))
  (defonce abdera-parser (.getParser abdera))
  (defonce abdera-client (AbderaClient.)))
