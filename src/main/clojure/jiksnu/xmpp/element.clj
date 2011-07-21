(ns jiksnu.xmpp.element
  (:use clj-tigase.core
        jiksnu.abdera
        jiksnu.namespace)
  (:require [clj-tigase.element :as element])
  (:import com.cliqset.abdera.ext.activity.ActivityEntry
           javax.xml.namespace.QName
           tigase.xml.Element
           tigase.server.Packet))

(declare abdera-to-tigase-element)

(defn add-children
  [^Element element abdera-element bound-namespaces]
  (doseq [child-element (.getElements abdera-element)]
    (.addChild element
               (abdera-to-tigase-element
                child-element bound-namespaces))))

(defn add-attributes
  [element abdera-element]
  (doseq [^QName attribute (.getAttributes abdera-element)]
    (let [^String value (.getAttributeValue abdera-element attribute)]
      (.addAttribute element (.getLocalPart attribute) value))))

(defn abdera-to-tigase-element
  "converts an abdera element to a tigase element"
  ([abdera-element]
     (abdera-to-tigase-element abdera-element {}))
  ([abdera-element namespace-map]
     (let [element (-> abdera-element get-qname element/make-element-qname)
           namespaces (.getNamespaces abdera-element)
           bound-namespaces (element/merge-namespaces element
                                              namespace-map
                                              namespaces)]
       (add-attributes element abdera-element)
       (add-children element abdera-element bound-namespaces)
       (if-let [text (.getText abdera-element)]
         (.setCData element text))
       element)))

(defn microblog-node?
  [^Element element]
  (= (element/node-value element) microblog-uri))

(defn vcard-query-ns?
  [^Element element]
  (= (.getXMLNS element) query-uri))

(defn vcard-publish?
  [^Element element]
  (and (= (.getName element) "publish")
       (= (.getXMLNS element) vcard-publish-uri)))

(defn inbox-node?
  [element]
  (= (element/node-value element) inbox-uri))

