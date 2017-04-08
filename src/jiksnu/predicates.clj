(ns jiksnu.predicates
  (:require [ciste.predicates :as pred]
            [clout.core :as clout]
            [compojure.core :as compojure]))

(defn name-path-matches?
  [request matcher]
  (if-let [path (:name matcher)]
    (let [request (assoc request :uri (:name request))
          pattern (clout/route-compile path)]
      (if-let [route-params (clout/route-matches pattern request)]
        (#'compojure/assoc-route-params request route-params)))))

(defn item-class-matches
  [request matcher]
  (when (= (:type matcher) (class (:item request)))
    request))

(def http
  [#'pred/request-method-matches?
   #'pred/path-matches?])

(def xmpp
  [#'pred/type-matches?
   #'pred/node-matches?
   #'pred/name-matches?
   #'pred/ns-matches?])

(defonce
  ^{:dynamic true
    :doc "The sequence of predicates used for command dispatch.
          By default, commands are dispatched by name."}
  *page-predicates*
  (ref [#'name-path-matches?]))

(defonce
  ^{:dynamic true
    :doc "The sequence of predicates used for command dispatch.
          By default, commands are dispatched by name."}
  *sub-page-predicates*
  (ref [#'item-class-matches
        #'pred/name-matches?]))

(defonce
  ^{:dynamic true}
  *page-matchers*
  (ref []))

(defonce
  ^{:dynamic true}
  *sub-page-matchers*
  (ref []))
