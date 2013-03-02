(ns jiksnu.predicates
  (:require [ciste.predicates :as pred]))

(def http
  [#'pred/request-method-matches?
   #'pred/path-matches?])

(def xmpp
  [#'pred/type-matches?
   #'pred/node-matches?
   #'pred/name-matches?
   #'pred/ns-matches?])
