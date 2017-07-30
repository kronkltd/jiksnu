(ns jiksnu.types
  (:require [clojure.spec.alpha :as s])
  (:import org.joda.time.DateTime))

(s/def ::date (partial instance? DateTime))
(s/def ::boolean boolean?)
;; FIXME: This won't match many domains
(s/def ::user-id #(re-matches #"acct:\w+@\w\.\w" %))
