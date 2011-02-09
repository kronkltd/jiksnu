(ns jiksnu.xmpp.view.following-view
  (:use jiksnu.model
        [jiksnu.namespace :only (pubsub-uri microblog-uri)]
        jiksnu.session
        jiksnu.xmpp.controller.following-controller
        [jiksnu.xmpp.view :only (make-element)]
        jiksnu.view
        ciste.core
        ciste.view)
  (:require [jiksnu.model.following :as following])
  (:import tigase.xml.Element))

(defn subscription-response-element
  [subscription]
  (make-element
   "subscription" {"node" microblog-uri
                   "subscription" "subscribed"
                   "created" (str (:created subscription))
                   "jid" (:to subscription)}))

(defn minimal-subscription-response
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  (make-element
   "pubsub" {"xmlns" pubsub-uri}
   [(make-element
     "subscriptions" {"node" microblog-uri}
     (map subscription-response-element subscriptions))]))
