(ns jiksnu.referrant
  (:require [clojure.tools.logging :as log]))

(defonce this (ref {}))
(defonce that (ref {}))

(defn get-this
  [k]
  (log/debugf "getting this %s" k)
  (get @this k))

(defn set-this
  [k v]
  (log/debugf "setting this %s to %s" k (prn-str v))
  (dosync
   (alter this assoc k v)))

(defn get-that
  [k]
  (log/debugf "getting that %s" k)
  (get @that k))

(defn set-that
  [k v]
  (log/debugf "setting that %s to %s" k (prn-str v))
  (dosync
   (alter that assoc k v)))

