(ns jiksnu.redis
  (:use aleph.redis
        (ciste [config :only (definitializer)])))

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

(definitializer
  (init-client)
  (init-stream))

(defn client
  [& args]
  (apply @*redis-client* args))

(defn redis-keys
  ([]
     (redis-keys "*"))
  ([pattern]
     @(client [:keys pattern])))

(defn queue-size
  [key]
  @(client [:llen key]))

(defn sadd
  [key val]
  (client [:sadd key val]))

(defn spop
  [key]
  @(client [:spop key]))
