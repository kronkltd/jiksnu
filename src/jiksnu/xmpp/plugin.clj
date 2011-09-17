(ns jiksnu.xmpp.plugin
  (:use (ciste [config :only (config)]
               [debug :only (spy)]
               routes)
        (clojure [pprint :only (pprint)])
        (jiksnu model
                routes
                view
                xmpp))
  (:require (clj-tigase [packet :as packet])
            (clojure.tools [logging :as log])
            (jiksnu [namespace :as namespace]))
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
                               :format :xmpp} request)
        route-fn (resolve-routes [xmpp-predicates] (lazier xmpp-routes))]
    (route-fn merged-request)))

(defn -process
  [this packet session
   repo queue settings]
  (with-database
    (if-let [to (.getStanzaTo packet)]
      (if-let [bare-to (.getBareJID to)]
        (let [request (packet/make-request packet)]
          (if (config :print :request)
            request)
          (if-let [response (main-handler queue request)]
            (do
              (.setPacketTo response (.getPacketFrom packet))
              (offer-packet queue response))))))))
