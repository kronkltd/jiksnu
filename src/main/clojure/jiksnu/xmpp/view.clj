(ns jiksnu.xmpp.view
  (:use clj-tigase.core
        clojure.contrib.logging
        [clojure.string :only (trim)]
        ciste.core
        jiksnu.namespace
        jiksnu.xmpp
        jiksnu.xmpp.element
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

(defn pubsub-element?
  [^Element element]
  (and element
       (= (.getName element) "pubsub")))

;; (defn packet?
;;   "Returns if the element is a packet"
;;   [^Element element]
;;   (instance? Packet element))

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

;; (defn process-child
;;   "adds content of the appropriate type to the element"
;;   [^Element element item]
;;   #_(println "item: " item)
;;   (if (element? item)
;;     (.addChild element item)
;;     (if (map? item)
;;       (.addChild element (to-tigase-element item))
;;       (if (vector? item)
;;         (if (seq item)
;;           (.addChild element (apply make-element item)))
;;         (if (string? item)
;;           (.setCData element (trim item))
;;           (if (coll? item)
;;             (doseq [i item]
;;               (process-child element i))))))))

;; (defn to-tigase-element
;;   "turns a map into a tigase element"
;;   [{:keys [tag attrs content]}]
;;   (let [attribute-names (into-array String (map name (keys attrs)))
;;         attribute-values (into-array String (vals attrs))
;;         tag-name (name tag)
;;         element (Element. tag-name attribute-names attribute-values)]
;;     (doseq [item content]
;;       (process-child element item))
;;     element))

;; (defn assign-namespace
;;   [^Element element
;;    namespace-map
;;    [k v]]
;;   (if (not= (get namespace-map k) v)
;;     (do (.addAttribute
;;          element (ns-prefix k) v)
;;         [k v])))

;; (defn element-name
;;   [name prefix]
;;   (str (if (not= prefix "")
;;          (str prefix ":"))
;;        name))

(defn ^Packet respond-with
  "given an item element, returns a packet"
  [request ^Element item]
  (.okResult (:packet request) item 0))

(defn make-jid
  ([user]
     (make-jid (:username user) (:domain user)))
  ([user domain]
     (make-jid user domain ""))
  ([user domain resource]
     (JID/jidInstance user domain resource)))

(defn deliver-packet!
  [^Packet packet]
  (try
    (.initVars packet)
    (.processPacket @*message-router* packet)
    (catch NullPointerException e
      (error "Router not started: " e)
      #_(stacktrace/print-stack-trace e)
      packet)))

(defn set-packet
  [request body]
  {:body body
   :from (:to request)
   :to (:from request)
   :id (:id request)
   :type :set})

(defn result-packet
  [request body]
  {:body body
   :from (:to request)
   :to (:from request)
   :id (:id request)
   :type :result})

(defmethod format-as :xmpp
  [format request response]
  response)

(defmethod serialize-as :xmpp
  [serialization elements]
  (make-packet elements))
