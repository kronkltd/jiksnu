(ns jiksnu.mock
  (:use jiksnu.factory
        jiksnu.file
        jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp
        jiksnu.xmpp.view)
  (:require [jiksnu.atom.view :as atom.view]
            [jiksnu.model.subscription :as subscription])
  (:import jiksnu.model.User
           org.apache.axiom.util.UIDGenerator
           #_tigase.db.AuthRepository
           tigase.db.UserRepository
           tigase.server.Packet
           tigase.server.xmppsession.SessionManagerHandler
           tigase.xmpp.XMPPResourceConnection
           tigase.xmpp.JID
           tigase.xmpp.BareJID))

(defn mock-user-repository
  []
    (proxy [UserRepository] []))

(defn mock-session-manager-handler
  []
  ;; TODO: implement
  (proxy [SessionManagerHandler] []))

(defn mock-activity-publish-request-element
  []
  (to-tigase-element (read-xml "activity-publish-request.xml")))

(defn mock-activity-query-request-element
  []
  (make-element
   "iq" {"type" "get"
         "id" (str (fseq :id))}
   [(make-element
     "pubsub" {"xmlns" pubsub-uri}
     [(make-element
       "items" {"node" microblog-uri} [])])]))

(defn mock-activity-query-request-with-id-element
  []
  (to-tigase-element (read-xml "activity-query-request-with-id.xml")))

(defn mock-inbox-query-request-element
  []
  (to-tigase-element (read-xml "inbox-query-request.xml")))

(defn mock-subscriber-publish-request-element
  []
  (to-tigase-element (read-xml "subscriber-publish-request.xml")))

(defn mock-subscriber-query-request-element
  []
  (to-tigase-element (read-xml "subscriber-query-request.xml")))

(defn mock-subscription-publish-request-element
  []
  (to-tigase-element (read-xml "subscription-publish-request.xml")))

(defn mock-subscription-query-request-element
  []
  (to-tigase-element (read-xml "subscription-query-request.xml")))

(defn mock-vcard-publish-request-element
  []
  (to-tigase-element (read-xml "vcard-publish-request.xml")))

(defn mock-vcard-query-request-element
  []
  (to-tigase-element (read-xml "vcard-query-request.xml")))

(defn mock-items-node
  []
  (children (mock-activity-query-request-element)))

(defn mock-activity-publish-request-packet
  []
  (let [element (mock-activity-publish-request-element)]
    (make-packet
     {:to (factory User)
      :from (factory User)
      :body element})))

(defn mock-activity-query-request-packet
  []
  (let [element (mock-activity-query-request-element)]
    (make-packet
     {:to (factory User)
      :from (factory User)
      :body element})))

(defn mock-activity-query-request-with-id-packet
  []
  (let [element (mock-activity-query-request-with-id-element)]
    (make-packet
     {:to (factory User)
      :from (factory User)
      :body element})))

(defn mock-inbox-query-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-inbox-query-request-element)}))

(defn mock-subscriber-publish-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-subscriber-publish-request-element)}))

(defn mock-subscriber-query-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-subscriber-query-request-element)}))

(defn mock-subscription-query-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-subscription-query-request-element)}))

(defn mock-subscription-publish-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-subscription-publish-request-element)}))

(defn mock-vcard-publish-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-vcard-publish-request-element)}))

(defn mock-vcard-query-request-packet
  []
  (make-packet
   {:to (factory User)
    :from (factory User)
    :body (mock-vcard-query-request-element)}))

(defn mock-activity-entry
  []
  (let [entry (atom.view/parse-xml-string (slurp-classpath "entry.xml"))]
    #_(.setId entry (UIDGenerator/generateURNString))
    entry))

(defn mock-activity-entries
  []
  (list (mock-activity-entry)))

(defn entry-map-json
  []
  (read-clojure "entry-json.clj"))

(defn extension-map
  []
  (read-clojure "extension.clj"))

(defn extension-with-attributes-map
  []
  (read-clojure "extension-with-attributes.clj"))

(defn extension-with-children-map
  []
  (read-clojure "extension-with-children.clj"))

(defn entry-map
  []
  (read-clojure "entry.clj"))
