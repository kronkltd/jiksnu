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
   '("iq" "message")))

(defn -supNamespaces
  "The namespaces this plugin is interested in"
  [this]
  (into-array
   String
   '("jabber:client" "jabber:client")))

(defn offer-packet
  [^Queue queue ^Packet packet]
  (debug (str "offering packet: " packet))
  (.offer queue packet))

(defn main-handler
  [queue request]
  (let [merged-request (merge {:serialization :xmpp
                               :format :xmpp} request)]
    (let [route-fn (resolve-routes (filter identity (lazier @*routes*)))]
      (if-let [response (route-fn merged-request)]
        response))))

(defn -process
  [this packet session
   repo queue settings]
  (with-database
    (if-let [to (.getStanzaTo packet)]
      (if-let [bare-to (.getBareJID to)]
        (let [request (make-request packet)]
          (if-let [response (main-handler queue request)]
            (do
              (.setPacketTo response (.getPacketFrom packet))
              (offer-packet queue response))))))))
