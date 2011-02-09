(ns jiksnu.mock
  (:use jiksnu.file
        jiksnu.model
        jiksnu.xmpp
        jiksnu.xmpp.view)
  (:require [jiksnu.atom.view :as atom.view]
            [jiksnu.model.subscription :as subscription]
            [jiksnu.model.vcard :as vcard])
  (:import org.apache.axiom.util.UIDGenerator
           #_tigase.db.AuthRepository
           tigase.db.UserRepository
           tigase.server.Packet
           tigase.server.xmppsession.SessionManagerHandler
           tigase.xmpp.XMPPResourceConnection
           tigase.xmpp.JID
           tigase.xmpp.BareJID))

(def #^:dynamic *recipient* "daniel@renfer.name")
(def #^:dynamic *sender* "renfer.name")

(defn mock-user-repository
  []
    (proxy [UserRepository] []))

(defn mock-session-manager-handler
  []
  ;; TODO: implement
  (proxy [SessionManagerHandler] []))

(defn mock-resource-connection
  "Returns a mocked session for an authorized session"
  [& {jid :jid
      authorized :authorized
      :as opts
      :or {jid (make-jid *sender*)
           authorized true}}]
  (proxy [XMPPResourceConnection] [nil nil nil nil]
    (getjid [] jid)
    (isAuthorized [] authorized)
    (isUserId [^BareJID other-jid]
      (= other-jid (.getBareJID jid)))))

(defn mock-activity-publish-request-element
  []
  (to-tigase-element (read-xml "activity-publish-request.xml")))

(defn mock-activity-query-request-element
  []
  (to-tigase-element (read-xml "activity-query-request.xml")))

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
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-activity-publish-request-element)}))

(defn mock-activity-query-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-activity-query-request-element)}))

(defn mock-activity-query-request-with-id-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-activity-query-request-with-id-element)}))

(defn mock-inbox-query-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-inbox-query-request-element)}))

(defn mock-subscriber-publish-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-subscriber-publish-request-element)}))

(defn mock-subscriber-query-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-subscriber-query-request-element)}))

(defn mock-subscription-query-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-subscription-query-request-element)}))

(defn mock-subscription-publish-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-subscription-publish-request-element)}))

(defn mock-vcard-publish-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
    :body (mock-vcard-publish-request-element)}))

(defn mock-vcard-query-request-packet
  []
  (make-packet
   {:to *recipient*
    :from *sender*
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
