(ns jiksnu.xmpp.plugin
  (:use jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp
        jiksnu.xmpp.routes
        jiksnu.xmpp.view
        ciste.core
        clojure.pprint
        clojure.contrib.logging)
  (:import java.util.Queue
           tigase.server.Packet
           tigase.xmpp.JID
           java.util.Map
           tigase.xml.Element
           tigase.xmpp.StanzaType
           tigase.xmpp.XMPPResourceConnection)
  (:gen-class
   :extends tigase.xmpp.XMPPProcessorAbstract))

(defn -id
  [this]
  "jiksnu")

(defn -supElements
  "The elements this plugin is inerested in"
  [this]
  (into-array
   String
   '("iq")))

(defn -supNamespaces
  "The namespaces this plugin is interested in"
  [this]
  (into-array
   String
   '("jabber:client")))

(defmethod default-format :atom
  [request response])

(defn offer-packet
  [^Queue queue ^Packet packet]
  (println "packet: " packet)
  (.offer queue packet))

(defmethod format-as :xmpp
  [format request response]
  response)

(defmethod serialize-as :xmpp
  [serialization elements]
  (make-packet elements))

(defn main-handler
  [queue request]
  (let [merged-request (merge {:serialization :xmpp
                               :format :xmpp} request)]
    (println "request: " merged-request)
    (let [route-fn (resolve-routes (filter identity (lazier @*routes*)))]
      (if-let [response (route-fn merged-request)]
        (do (println " ")
            response)))))

(defn -process
  [this packet session
   repo queue settings]
  (println " ")
  (if-let [to (.getStanzaTo packet)]
    (if-let [bare-to (.getBareJID to)]
      (with-database
        (let [request (make-request packet)]
          (if-let [response (main-handler queue request)]
            (do
              (println "from: " (.getStanzaFrom packet))
              (.setPacketTo response (.getPacketFrom packet))
              #_(let [connection-id (.getConnectionId session (:to request))]
                  (.setPacketTo response connection-id))
                (offer-packet queue response))))))))
