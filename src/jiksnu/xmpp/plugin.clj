(ns jiksnu.xmpp.plugin
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        ciste.routes
        [clojure.pprint :only [pprint]]
        jiksnu.model
        jiksnu.routes)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as namespace])
  (:import java.util.Queue
           tigase.server.Packet
           tigase.xmpp.JID
           java.util.Map
           tigase.xml.Element
           tigase.xmpp.StanzaType
           tigase.xmpp.XMPPProcessor
           tigase.xmpp.XMPPResourceConnection)
  (:gen-class
   :extends tigase.xmpp.XMPPProcessorAbstract))

(defn -id
  [^XMPPProcessor this]
  "jiksnu")

(defn -supElements
  "The elements this plugin is inerested in"
  [^XMPPProcessor this]
  (into-array
   String
   '("iq" "message")))

(defn -supNamespaces
  "The namespaces this plugin is interested in"
  [^XMPPProcessor this]
  (into-array
   String
   '("jabber:client" "jabber:client")))

(defn offer-packet
  [^Queue queue ^Packet packet]
  (log/debug (str "offering packet: " packet))
  (.offer queue packet))

(defn main-handler
  [queue request]
  (let [merged-request (merge {:serialization :xmpp
                               :format :xmpp} request)
        route-fn (resolve-routes [xmpp-predicates] (lazier xmpp-routes))]
    (route-fn merged-request)))

(defn -process
  [this ^Packet packet session
   repo queue settings]
  (if (config :print :packet)
    (spy packet))
  (let [packet-to (.getPacketTo packet)]
    (if-not (#{"sess-man"} (.getLocalpart packet-to))
      (if-let [to (.getStanzaTo packet)]
        (if-let [bare-to (.getBareJID to)]
          (let [request (packet/make-request packet)]
            (when (config :print :request)
              (spy request))
            (if-let [response (main-handler queue request)]
              (do
                (.setPacketTo response (.getPacketFrom packet))
                (offer-packet queue response)))))))))
