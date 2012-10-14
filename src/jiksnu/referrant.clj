(ns jiksnu.referrant)

(defonce this (ref {}))
(defonce that (ref {}))

(defn get-this
  [k]
  (get @this k))

(defn set-this
  [k v]
  (dosync
   (alter this assoc k v)))

(defn get-that
  [k]
  (get @that k))

(defn set-that
  [k v]
  (dosync
   (alter that assoc k v)))

