(ns jiksnu.mock
  (:use ciste.factory
        ciste.view
        jiksnu.file
        jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp
        jiksnu.xmpp.view)
  (:require [jiksnu.atom.view :as atom.view]
            [jiksnu.model.subscription :as subscription])
  (:import jiksnu.model.Activity
           jiksnu.model.User
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
  (let [activity (factory Activity)]
    (make-element
     "pubsub"  {"xmlns" pubsub-uri}
     ["items" {"node" microblog-uri}
      (with-serialization :xmpp
        (with-format :atom
          (abdera-to-tigase-element (show-section activity))
          ))
     ]
    ))
  #_(to-tigase-element (read-xml "activity-publish-request.xml")))

(defn mock-activity-query-request-element
  []
  (make-element
   "pubsub" {"xmlns" pubsub-uri}))
