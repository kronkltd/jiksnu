(ns jiksnu.xmpp.view.follower-view
  (:use jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.xmpp.controller.follower-controller
        jiksnu.xmpp.view
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [jiksnu.model.follower :as follower])
  (:import tigase.xml.Element))

(defn subscriber-response-element
  [subscriber]
  (make-element
   "subscriber" {"node" microblog-uri
                 "created" (str (:created subscriber))
                 "jid" (str (:from subscriber))}))

(defn minimal-subscriber-response
  [subscribers]
  (make-element
   "pubsub" {"xmlns" pubsub-uri}
   [(make-element
     "subscribers" {"node" microblog-uri}
     (map subscriber-response-element subscribers))]))

(defview #'index :xmpp
  [request subscribers]
  (println "subscribers: " subscribers)
  {:body (minimal-subscriber-response nil)
   :from (:to request)
   :to (:from request)})
