(ns jiksnu.xmpp.routes
  (:use jiksnu.namespace
        clojure.contrib.logging
        ciste.core)
  (:require (jiksnu.xmpp.controller
             [activity-controller :as activity]
             [subscription-controller :as subscription]
             [user-controller :as user])
            [jiksnu.xmpp.view :as view]
            compojure.core
            clout.core)
  (:import tigase.xmpp.StanzaType))

(dosync
 (ref-set
  *routes*
  (map
   (fn [[m a]]
     [(merge {:serialization :xmpp
              :format :xmpp} m) a])
   [[{:method :get
      :pubsub true
      :name "items"
      :node microblog-uri}
     #'activity/index]

    [{:method :put
      :pubsub true
      :name "items"
      :node microblog-uri}
     #'activity/create]

    [{:method :get
      :pubsub true
      :node inbox-uri}
     #'user/inbox]

    [{:method :get
      :name "query"
      :ns query-uri}
     #'user/show]

    [{:method :put
      :name "publish"
      :ns vcard-uri}
     #'user/create]

    [{:method :get
      :name "subscriptions"}
     #'subscription/subscriptions]

    [{:method :put
      :name "subscriptions"}
     #'subscription/subscribe]

    [{:method :get
      :name "subscribers"}
     #'subscription/subscribers]

    [{:method :put
      :name "subscribers"}
     #'subscription/subscribed]])))

(defn node-matches?
  [request matcher]
  (if (:node matcher)
    (if (= (:node request) (:node matcher))
      request)
    request))

(defn type-matches?
  [request matcher]
  (if (:method matcher)
    (if (= (:method matcher) (:method request))
      request)
    request))

(defn name-matches?
  [request matcher]
  (if (:name matcher)
    (if (= (:name matcher) (:name request))
      request)
    request))

(defn ns-matches?
  [request matcher]
  (if (:ns matcher)
        (if (= (:ns matcher) (:ns request))
      request)
    request))

(defn http-serialization?
  [request matcher]
  (if (= (:serialization request) :http)
    request))

(defn xmpp-serialization?
  [request matcher]
  (if (= (:serialization request) :xmpp)
    request))

(defn request-method-matches?
  [request matcher]
  (if (#'compojure.core/method-matches (:method matcher) request)
    request))

(defn path-matches?
  [request matcher]
  (let [prepared (#'compojure.core/prepare-route (:path matcher))]
    (if-let [route-params (#'clout.core/route-matches prepared request)]
      (#'compojure.core/assoc-route-params request route-params))))

(dosync
 (ref-set *matchers*
          [[#'xmpp-serialization?
            [#'type-matches?
             #'node-matches?
             #'name-matches?
             #'ns-matches?]]
           [#'http-serialization?
            [#'request-method-matches?
             #'path-matches?]]]))

(def #^:dynamic *standard-middleware*
  [])
