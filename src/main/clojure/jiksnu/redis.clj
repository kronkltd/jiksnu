(ns jiksnu.redis
  (:use aleph.redis)
  )

(defonce ^:dynamic *redis-client* (ref nil))
(defonce ^:dynamic *redis-stream* (ref nil))

(defn init-client
  []
  (dosync
   (ref-set *redis-client* (redis-client {:host "localhost"}))))

(defn init-stream
  []
  (dosync
   (ref-set *redis-stream* (redis-stream {:host "localhost"}))))

(defn redis-keys
  []
  @(@*redis-client* [:keys "*"])
  )

(defn queue-size
  [key]
  @(@*redis-client* [:llen key]))

