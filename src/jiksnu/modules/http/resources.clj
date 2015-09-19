(ns jiksnu.modules.http.resources
  (:require [clojure.tools.logging :as log]
            [octohipster.core :as octo]))

(defonce groups
  ;; "Ref holding each api group"
  (ref {}))

(defonce resources (ref {}))

(defmacro defresource
  [group name & opts]
  `(do
     (declare ~name)
     (log/debugf "defining resource: %s" (var ~name))
     (octo/defresource ~name
       ~@opts)

     (dosync
      (alter resources assoc-in [(var ~group) (var ~name)] ~name))))


(defmacro defgroup
  [name & opts]
  `(do
     (declare ~name)
     (octo/defgroup ~name
       ~@opts)

     (dosync
      (alter groups assoc (var ~name) ~name))))

