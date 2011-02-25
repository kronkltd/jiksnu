(ns jiksnu.xmpp.view
  (:use clojure.contrib.logging
        [clojure.string :only (trim)]
        jiksnu.namespace
        jiksnu.xmpp
        jiksnu.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [clojure.stacktrace :as stacktrace])
  (:import com.cliqset.abdera.ext.activity.ActivityEntry
           javax.xml.namespace.QName
           jiksnu.model.Activity
           tigase.server.Packet
           tigase.xml.Element
           tigase.xmpp.JID
           tigase.xmpp.StanzaType))

(declare to-tigase-element)
(declare abdera-to-tigase-element)

(def #^:dynamic *packet*)

(defn node-value
  [^Element element]
  (.getAttribute element "node"))

(defn children
  "returns the child elements of the given element"
  ([^Element element]
     (if element
       (seq (.getChildren element))))
  ([^Packet packet path]
     (if packet
       (seq (.getElemChildren packet path)))))

(defn ns-prefix
  [k]
  (apply str
         "xmlns"
         (if (not= k "")
           (list ":" k))))

(defn element?
  "Returns if the argument is an element"
  [arg]
  (instance? Element arg))

(defn pubsub-element?
  [^Element element]
  (and element
       (= (.getName element) "pubsub")))

(defn packet?
  "Returns if the element is a packet"
  [^Element element]
  (instance? Packet element))

(defn iq-elements
  [^Packet packet]
  (children packet "/iq"))

(defn pubsub-items
  "Returns a seq of pubsub elements contained in a packet"
  [^Packet packet]
  (children packet "/iq/pubsub"))

(defn bare-recipient?
  [^Packet packet]
  (if packet
    (let [recipient-jid (.getStanzaTo packet)]
     (= recipient-jid (.copyWithoutResource recipient-jid)))))

#_(defn from-authenticated?
  [^Packet packet]
  (if packet
    (let [sender-jid (.getStanzaFrom packet)]
     (session/is-user? (.getBareJID sender-jid)))))

(defn get-items
  [^Packet packet]
  (if-let [node (first (pubsub-items packet))]
    (children node)))

(defn make-request
  [^Packet packet]
  (let [type (keyword (str (.getType packet)))
        to (.getStanzaTo packet)
        from (.getStanzaFrom packet)
                payload  (first (iq-elements packet))
        pubsub? (pubsub-element? payload)
        child-node (first (children payload))
        node (and child-node (node-value child-node))
        name (if pubsub?
               (if child-node (.getName child-node))
               (if payload (.getName payload)))]
    {:to to
     :from from
     :pubsub pubsub?
     :payload payload
     :id (.getAttribute packet "id")
     :name name
     :node node
     :ns (if payload (.getXMLNS payload))
     :packet packet
     :request-method type
     :method type
     :items (get-items packet)}))

(declare make-element)

(defn process-child
  "adds content of the appropriate type to the element"
  [^Element element item]
  #_(println "item: " item)
  (if (element? item)
    (.addChild element item)
    (if (map? item)
      (.addChild element (to-tigase-element item))
      (if (vector? item)
        (if (seq item)
          (.addChild element (apply make-element item)))
        (if (string? item)
          (.setCData element (trim item))
          (if (coll? item)
            (doseq [i item]
              (process-child element i))))))))

(defn ^Element to-tigase-element
  "turns a map into a tigase element"
  [{:keys [tag attrs content]}]
  (let [attribute-names (into-array String (map name (keys attrs)))
        attribute-values (into-array String (vals attrs))
        tag-name (name tag)
        element (Element. tag-name attribute-names attribute-values)]
    (doseq [item content]
      (process-child element item))
    element))

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

(defn abdera-to-tigase-element
  "converts an abdera element to a tigase element"
  ([abdera-element]
     (abdera-to-tigase-element abdera-element {}))
  ([abdera-element namespace-map]
     (let [element (-> abdera-element get-qname make-element-qname)]
       (let [namespaces (.getNamespaces abdera-element)]
         (let [bound-namespaces (merge-namespaces element
                                                 namespace-map
                                                 namespaces)]
          (add-attributes element abdera-element)
          (add-children element abdera-element bound-namespaces)
          (if-let [text (.getText abdera-element)]
            (.setCData element text))
          element)))))

(defn ^Element make-element
  "Create a tigase element"
  ([name]
     (make-element name {} nil))
  ([name attrs]
     (make-element name attrs nil))
  ([name attrs & children]
     (let [element (Element. name)]
       (doseq [[attr val] attrs]
         (.addAttribute element attr val))
       #_(println "children: " children)
       (doseq [child children]
         (process-child element child))
       element)))

(defn ^Packet respond-with
  "given an item element, returns a packet"
  [request ^Element item]
  (.okResult (:packet request) item 0))

(defn make-jid
  ([user]
     ;; (println "user: " user)
     (make-jid (:username user) (:domain user)))
  ([user domain]
     (make-jid user domain ""))
  ([user domain resource]
     (JID/jidInstance user domain resource)))

(defn make-packet
  [packet-map]
  ;; (println "packet-map: " packet-map)
  (let [packet
        (Packet/packetInstance
         (:body packet-map)
         (:from packet-map)
         (:to packet-map))]
    ;; (println "making packet: " packet)
    packet))

(defn deliver-packet!
  [packet]
  (try
    (.processPacket @*message-router* packet)
    (catch NullPointerException e
      (error "Router not started: " e)
      #_(stacktrace/print-stack-trace e)
      packet)))
