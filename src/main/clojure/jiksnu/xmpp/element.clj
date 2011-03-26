(ns jiksnu.xmpp.element
  (:use [clojure.string :only (trim)]
        jiksnu.namespace)
  (:import com.cliqset.abdera.ext.activity.ActivityEntry
           javax.xml.namespace.QName
           tigase.xml.Element
           tigase.server.Packet))

(declare abdera-to-tigase-element)
(declare to-tigase-element)

(defn process-child
  "adds content of the appropriate type to the element"
  [^Element element item]
  (if (map? item)
    (.addChild element (to-tigase-element item))
    (if (string? item)
      (.setCData element (trim item)))))

(defn to-tigase-element
  [{:keys [tag attrs content]}]
  (let [attribute-names (into-array String (map name (keys attrs)))
        attribute-values (into-array String (vals attrs))
        tag-name (name tag)
        element (Element. tag-name attribute-names attribute-values)]
    (doseq [item content]
      (process-child element item))
    element))

(defn element?
  "Returns if the argument is an element"
  [arg]
  (instance? Element arg))

(defn children
  [#^Element element]
  (seq (.getChildren element)))

(defn ns-prefix
  [k]
  (apply str
         "xmlns"
         (if (not= k "")
           (list ":" k))))

(defn assign-namespace
  [^Element element
   namespace-map
   [k v]]
  (if (not= (get namespace-map k) v)
    (do (.addAttribute
         element (ns-prefix k) v)
        [k v])))

(defn element-name
  [name prefix]
  (str (if (not= prefix "")
         (str prefix ":"))
       name))

(defn add-children
  [^Element element abdera-element bound-namespaces]
  (doseq [child-element (.getElements abdera-element)]
    (.addChild element
               (abdera-to-tigase-element
                child-element bound-namespaces))))

(defn add-attributes
  [^Element element abdera-element]
  (doseq [attribute (.getAttributes abdera-element)]
    (let [value (.getAttributeValue abdera-element attribute)]
      (.addAttribute element (.getLocalPart attribute) value))))

(defn parse-qname
  [^QName qname]
  {:name (.getLocalPart qname)
   :prefix (.getPrefix qname)})

(defn merge-namespaces
  [^Element element
   namespace-map
   namespaces]
  (merge namespace-map
         (into {}
               (map
                (partial assign-namespace element namespace-map)
                namespaces))))

(defn get-qname
  "Returns a map representing the QName of the given element"
  [element]
  (parse-qname (.getQName element)))

(defn make-element-qname
  [{:keys [name prefix]}]
  (Element. (element-name name prefix)))

(defn node-value
  [#^Element element]
  (.getAttribute element "node"))

(defn microblog-node?
  [#^Element element]
  (= (node-value element) microblog-uri))

(defn vcard-query-ns?
  [#^Element element]
  (= (.getXMLNS element) query-uri))

(defn vcard-publish?
  [#^Element element]
  (and (= (.getName element) "publish")
       (= (.getXMLNS element) vcard-publish-uri)))

(defn inbox-node?
  [element]
  (= (node-value element) inbox-uri))

(defn packet?
  "Returns if the element is a packet"
  [element]
  (instance? Packet element))
