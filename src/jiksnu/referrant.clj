(ns jiksnu.referrant
  (:require [taoensso.timbre :as timbre]))

(defonce this (ref {}))
(defonce that (ref {}))

(defn get-this
  [k]
  (timbre/debugf "getting this %s" k)
  (get @this k))

(defn set-this
  [k v]
  (timbre/debugf "setting this %s to %s" k (prn-str v))
  (dosync
   (alter this assoc k v)))

(defn get-that
  [k]
  (timbre/debugf "getting that %s" k)
  (get @that k))

(defn set-that
  [k v]
  (timbre/debugf "setting that %s to %s" k (prn-str v))
  (dosync
   (alter that assoc k v)))
